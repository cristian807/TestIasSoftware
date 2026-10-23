package com.ias_software.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;

import com.ias_software.test.application.dto.request.ProcessUsageRequest;
import com.ias_software.test.application.dto.response.UsageRequestResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UsageRequestApiIT {

	private static final String BASE_PATH = "/api/v1/usage-requests";

	@Value("${local.server.port}")
	private int port;

	@Autowired
	private DatabaseClient databaseClient;

	private WebTestClient client;

	@BeforeEach
	void resetData() {
		client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
		databaseClient.sql("DELETE FROM usage_requests").then()
				.then(databaseClient.sql("UPDATE pre_approved SET available_amount = 1000000 WHERE id = 'PRA-1001'").then())
				.then(databaseClient.sql("UPDATE pre_approved SET available_amount = 800000 WHERE id = 'PRA-1002'").then())
				.then(databaseClient.sql("UPDATE pre_approved SET available_amount = 2000000 WHERE id = 'PRA-2001'").then())
				.block();
	}

	private Map<String, Object> body(String reference, String preApprovedId, String customerId, Object amount) {
		return Map.of("requestReference", reference, "preApprovedId", preApprovedId,
				"customerId", customerId, "amount", amount);
	}

	private BigDecimal availableOf(String preApprovedId) {
		return databaseClient.sql("SELECT available_amount FROM pre_approved WHERE id = :id")
				.bind("id", preApprovedId)
				.map(row -> row.get("available_amount", BigDecimal.class))
				.one()
				.block();
	}

	private Long countRequests() {
		return databaseClient.sql("SELECT COUNT(*) AS total FROM usage_requests")
				.map(row -> row.get("total", Long.class))
				.one()
				.block();
	}


	@Test
	void concurrentRequestsNeverAuthorizeMoreThanTheAvailableLimit() {
		WebClient webClient = WebClient.create("http://localhost:" + port);

		List<UsageRequestResponse> responses = Flux.range(1, 12)
				.flatMap(i -> post(webClient, ProcessUsageRequest.builder()
						.requestReference("REF-C" + i)
						.preApprovedId("PRA-1001")
						.customerId("USR-10")
						.amount(new BigDecimal("300000"))
						.build()), 12)
				.collectList()
				.block();

		long authorized = responses.stream().filter(r -> "AUTHORIZED".equals(r.getStatus())).count();
		long rejected = responses.stream().filter(r -> "REJECTED".equals(r.getStatus())).count();

		assertThat(authorized).isEqualTo(3);
		assertThat(rejected).isEqualTo(9);
		assertThat(availableOf("PRA-1001")).isEqualByComparingTo("100000");
		assertThat(countRequests()).isEqualTo(12L);
	}

    @Test
    void repeatedReferenceReturnsOriginalResultAndDebitsOnlyOnce() {
        client.post().uri(BASE_PATH).bodyValue(body("REF-010", "PRA-1001", "USR-10", 600000))
                .exchange().expectStatus().isCreated();

        client.post().uri(BASE_PATH).bodyValue(body("REF-010", "PRA-1001", "USR-10", 600000))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.status").isEqualTo("AUTHORIZED");

        assertThat(availableOf("PRA-1001")).isEqualByComparingTo("400000");
        assertThat(countRequests()).isEqualTo(1L);
    }

    @Test
    void repeatedReferenceWithDifferentDataIsAConflictAndKeepsTheOriginal() {
        client.post().uri(BASE_PATH).bodyValue(body("REF-011", "PRA-1001", "USR-10", 600000))
                .exchange().expectStatus().isCreated();

        client.post().uri(BASE_PATH).bodyValue(body("REF-011", "PRA-1001", "USR-10", 100000))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody().jsonPath("$.status").isEqualTo(409);

        client.get().uri(BASE_PATH + "/REF-011")
                .exchange()
                .expectBody().jsonPath("$.amount").isEqualTo(600000);

        assertThat(availableOf("PRA-1001"))
                .isEqualByComparingTo("400000");
    }

    @Test
    void concurrentRepeatedReferenceIsProcessedOnce() {
        WebClient webClient = WebClient.create("http://localhost:" + port);

        List<UsageRequestResponse> responses = Flux.range(1, 10)
                .flatMap(i -> post(webClient, ProcessUsageRequest.builder()
                        .requestReference("REF-SAME")
                        .preApprovedId("PRA-1001")
                        .customerId("USR-10")
                        .amount(new BigDecimal("250000"))
                        .build()), 10)
                .collectList()
                .block();

        assertThat(responses).hasSize(10).allSatisfy(r -> assertThat(r.getStatus()).isEqualTo("AUTHORIZED"));
        assertThat(availableOf("PRA-1001")).isEqualByComparingTo("750000");
        assertThat(countRequests()).isEqualTo(1L);
    }

    private Mono<UsageRequestResponse> post(WebClient webClient, ProcessUsageRequest request) {
        return webClient.post().uri(BASE_PATH).bodyValue(request)
                .retrieve()
                .bodyToMono(UsageRequestResponse.class);
    }
}
