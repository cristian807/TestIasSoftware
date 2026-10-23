package com.ias_software.test.infrastructure.adapter;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.ias_software.test.domain.model.PreApproved;
import com.ias_software.test.domain.port.PreApprovedRepository;
import com.ias_software.test.infrastructure.mapper.PreApprovedPersistenceMapper;
import com.ias_software.test.infrastructure.persistence.repository.PreApprovedR2dbcRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class PreApprovedRepositoryAdapter implements PreApprovedRepository {

	private final PreApprovedR2dbcRepository repository;
	private final PreApprovedPersistenceMapper mapper;

	@Override
	public Mono<PreApproved> findById(String id) {
        return repository.findById(id).map(mapper::toDomain);
	}

	@Override
	public Mono<Boolean> debitIfAvailable(String id, BigDecimal amount) {
		return repository.debitIfAvailable(id, amount)
                .map(updated -> updated > 0);
	}
}
