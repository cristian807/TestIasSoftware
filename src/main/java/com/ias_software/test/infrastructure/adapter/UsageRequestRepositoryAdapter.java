package com.ias_software.test.infrastructure.adapter;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import com.ias_software.test.domain.exception.DuplicateRequestReferenceException;
import com.ias_software.test.domain.model.UsageRequest;
import com.ias_software.test.domain.port.UsageRequestRepository;
import com.ias_software.test.infrastructure.mapper.UsageRequestPersistenceMapper;
import com.ias_software.test.infrastructure.persistence.repository.UsageRequestR2dbcRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UsageRequestRepositoryAdapter implements UsageRequestRepository {

	private final UsageRequestR2dbcRepository repository;
	private final UsageRequestPersistenceMapper mapper;

	@Override
	public Mono<UsageRequest> findByReference(String requestReference) {
		return repository.findByRequestReference(requestReference).map(mapper::toDomain);
	}

	@Override
	public Flux<UsageRequest> findRecent(int limit) {
        return repository.findRecent(limit)
                .map(mapper::toDomain);
	}

	@Override
	public Mono<UsageRequest> save(UsageRequest usageRequest) {
		return repository.save(mapper.toEntity(usageRequest))
				.map(mapper::toDomain)
				.onErrorMap(DataIntegrityViolationException.class,
						ex -> new DuplicateRequestReferenceException(usageRequest.getRequestReference(), ex));
	}
}
