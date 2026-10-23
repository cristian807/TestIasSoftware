package com.ias_software.test.infrastructure.persistence.repository;

import java.math.BigDecimal;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.ias_software.test.infrastructure.persistence.entity.PreApprovedEntity;

import reactor.core.publisher.Mono;

public interface PreApprovedR2dbcRepository extends ReactiveCrudRepository<PreApprovedEntity, String> {

	@Modifying
	@Query("""
			UPDATE pre_approved
			   SET available_amount = available_amount - :amount
			 WHERE id = :id
			   AND status = 'ACTIVE'
			   AND available_amount >= :amount
			""")
	Mono<Long> debitIfAvailable(
            @Param("id") String id,
            @Param("amount") BigDecimal amount
    );
}
