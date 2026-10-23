package com.ias_software.test.domain.port;

import com.ias_software.test.domain.model.UsageRequest;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UsageRequestRepository {

	Mono<UsageRequest> findByReference(String requestReference);

	Flux<UsageRequest> findRecent(int limit);

	Mono<UsageRequest> save(UsageRequest usageRequest);
}
