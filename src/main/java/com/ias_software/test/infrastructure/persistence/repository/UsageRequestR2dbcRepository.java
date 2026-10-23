package com.ias_software.test.infrastructure.persistence.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.ias_software.test.infrastructure.persistence.entity.UsageRequestEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UsageRequestR2dbcRepository extends ReactiveCrudRepository<UsageRequestEntity, Long> {

	Mono<UsageRequestEntity> findByRequestReference(String requestReference);

	@Query("SELECT * FROM usage_requests ORDER BY processed_at DESC, id DESC LIMIT :limit")
	Flux<UsageRequestEntity> findRecent(
            @Param("limit") int limit
    );
}
