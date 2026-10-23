package com.ias_software.test.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import com.ias_software.test.domain.model.RejectionReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ias_software.test.application.dto.request.ProcessUsageRequest;
import com.ias_software.test.application.mapper.UsageRequestResponseMapper;
import com.ias_software.test.domain.model.PreApproved;
import com.ias_software.test.domain.model.PreApprovedStatus;
import com.ias_software.test.domain.model.UsageRequest;
import com.ias_software.test.domain.port.PreApprovedRepository;
import com.ias_software.test.domain.port.TransactionalExecutor;
import com.ias_software.test.domain.port.UsageRequestRepository;
import com.ias_software.test.domain.service.UsageEvaluationService;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class UsageRequestServiceTest {

	private static final Instant NOW = Instant.parse("2026-10-22T15:00:00Z");

	@Mock
	private PreApprovedRepository preApprovedRepository;
	@Mock
	private UsageRequestRepository usageRequestRepository;

	private UsageRequestService service;

	@BeforeEach
	void setUp() {
		TransactionalExecutor passThrough = new TransactionalExecutor() {
			@Override
			public <T> Mono<T> execute(Mono<T> action) {
				return action;
			}
		};
		service = new UsageRequestService(preApprovedRepository, usageRequestRepository, passThrough,
				new UsageEvaluationService(), new UsageRequestResponseMapper(), Clock.fixed(NOW, ZoneOffset.UTC));
	}



	@Test
	void authorizesAndDebitsWhenRulesAreMet() {
		when(usageRequestRepository.findByReference("REF-001")).thenReturn(Mono.empty());
		when(preApprovedRepository.findById("PRA-1001")).thenReturn(Mono.just(activePreApproved("1000000")));
		when(preApprovedRepository.debitIfAvailable("PRA-1001", new BigDecimal("600000"))).thenReturn(Mono.just(true));
        simuleSave();

		StepVerifier.create(service.process(request("REF-001", "PRA-1001", "USR-10", "600000")))
				.assertNext(result -> {
					assertThat(result.isDuplicate()).isFalse();
					assertThat(result.getRequest().getStatus()).isEqualTo("AUTHORIZED");
					assertThat(result.getRequest().getReasonCode()).isNull();
					assertThat(result.getRequest().getProcessedAt()).isEqualTo(NOW);
				})
				.verifyComplete();
	}

	@Test
	void rejectedRequestIsPersistedWithoutTouchingTheLimit() {
		when(usageRequestRepository.findByReference("REF-002"))
                .thenReturn(Mono.empty());

		when(preApprovedRepository.findById("PRA-1002"))
                .thenReturn(Mono.just(
                        PreApproved.builder()
                        .id("PRA-1002")
                        .customerId("USR-10")
                        .status(PreApprovedStatus.BLOCKED)
                        .availableAmount(new BigDecimal("800000"))
                        .build()
                ));
        simuleSave();

		StepVerifier.create(service.process(request("REF-002", "PRA-1002", "USR-10", "100000")))
				.assertNext(result -> {
					assertThat(result.getRequest().getStatus()).isEqualTo("REJECTED");
					assertThat(result.getRequest().getReasonCode()).isEqualTo("PRE_APPROVED_NOT_ENABLED");
				})
				.verifyComplete();

		verify(preApprovedRepository, never()).debitIfAvailable(anyString(), any());
	}



    private ProcessUsageRequest request(String reference, String preApprovedId, String customer, String amount) {
        return ProcessUsageRequest.builder()
                .requestReference(reference)
                .preApprovedId(preApprovedId)
                .customerId(customer)
                .amount(new BigDecimal(amount))
                .build();
    }

    @Test
    void unknownPreApprovedIsRejectedAndStored() {
        when(usageRequestRepository.findByReference("REF-003")).thenReturn(Mono.empty());
        when(preApprovedRepository.findById("PRA-9999")).thenReturn(Mono.empty());
        simuleSave();

        StepVerifier.create(service.process(request("REF-003", "PRA-9999", "USR-10", "1000")))
                .assertNext(result -> assertThat(result.getRequest().getReasonCode())
                        .isEqualTo(RejectionReason.PRE_APPROVED_NOT_FOUND.name()))
                .verifyComplete();
    }

    private PreApproved activePreApproved(String available) {
        return PreApproved.builder()
                .id("PRA-1001")
                .customerId("USR-10")
                .status(PreApprovedStatus.ACTIVE)
                .availableAmount(new BigDecimal(available))
                .build();
    }

    private void simuleSave() {
        when(usageRequestRepository.save(any(UsageRequest.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
    }
}
