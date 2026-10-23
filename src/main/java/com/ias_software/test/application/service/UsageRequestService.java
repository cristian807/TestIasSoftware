package com.ias_software.test.application.service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ias_software.test.application.dto.request.ProcessUsageRequest;
import com.ias_software.test.application.dto.response.UsageProcessingResult;
import com.ias_software.test.application.dto.response.UsageRequestResponse;
import com.ias_software.test.application.mapper.UsageRequestResponseMapper;
import com.ias_software.test.domain.exception.DuplicateRequestReferenceException;
import com.ias_software.test.domain.exception.ReferenceConflictException;
import com.ias_software.test.domain.exception.UsageRequestNotFoundException;
import com.ias_software.test.domain.model.PreApproved;
import com.ias_software.test.domain.model.RejectionReason;
import com.ias_software.test.domain.model.UsageRequest;
import com.ias_software.test.domain.port.PreApprovedRepository;
import com.ias_software.test.domain.port.TransactionalExecutor;
import com.ias_software.test.domain.port.UsageRequestRepository;
import com.ias_software.test.domain.service.UsageEvaluationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsageRequestService {

	static final int DEFAULT_RECENT_LIMIT = 20;
	static final int MAX_RECENT_LIMIT = 100;

	private final PreApprovedRepository preApprovedRepository;
	private final UsageRequestRepository usageRequestRepository;
	private final TransactionalExecutor transactionalExecutor;
	private final UsageEvaluationService evaluationService;
	private final UsageRequestResponseMapper responseMapper;
	private final Clock clock;

	public Mono<UsageProcessingResult> process(ProcessUsageRequest request) {
		ProcessUsageRequest normalized = normalize(request);

		return usageRequestRepository.findByReference(normalized.getRequestReference())
				.map(existing -> replay(existing, normalized))
				.switchIfEmpty(Mono.defer(() -> registerNew(normalized)));
	}

	public Mono<UsageRequestResponse> findByReference(String requestReference) {
		return usageRequestRepository.findByReference(requestReference.trim())
				.switchIfEmpty(Mono.error(() -> new UsageRequestNotFoundException(requestReference)))
				.map(responseMapper::toResponse);
	}

	public Flux<UsageRequestResponse> findRecent(Integer limit) {
		int size = limit == null ? DEFAULT_RECENT_LIMIT : Math.max(1, Math.min(limit, MAX_RECENT_LIMIT));
		return usageRequestRepository.findRecent(size)
                .map(responseMapper::toResponse);
	}

    //Si no existe la referencia registramos una nueva
	private Mono<UsageProcessingResult> registerNew(ProcessUsageRequest request) {
		return transactionalExecutor.execute(evaluateAndSave(request))
				.map(saved -> toResult(saved, false))
				.onErrorResume(DuplicateRequestReferenceException.class, ex -> {
					// otra peticion con la misma referencia gano la carrera; la transaccion propia ya hizo rollback
					log.info("Referencia {} registrada concurrentemente, se devuelve la original", request.getRequestReference());
					return usageRequestRepository.findByReference(request.getRequestReference())
							.map(existing -> replay(existing, request))
							.switchIfEmpty(Mono.error(ex));
				});
	}

	private Mono<UsageRequest> evaluateAndSave(ProcessUsageRequest request) {
		return preApprovedRepository.findById(request.getPreApprovedId())
				.map(Optional::of)
				.defaultIfEmpty(Optional.<PreApproved>empty())
				.flatMap(found -> {
					Optional<RejectionReason> rejection = evaluationService.evaluate(
							request.getCustomerId(), request.getAmount(), found);
					if (rejection.isPresent()) {
						return saveRejected(request, rejection.get());
					}
					return preApprovedRepository.debitIfAvailable(request.getPreApprovedId(), request.getAmount())
							.flatMap(debited -> debited
									? saveAuthorized(request)
									: saveRejected(request, RejectionReason.INSUFFICIENT_FUNDS));
				});
	}

	private Mono<UsageRequest> saveAuthorized(ProcessUsageRequest request) {
		return usageRequestRepository.save(UsageRequest.authorized(
				request.getRequestReference(), request.getPreApprovedId(), request.getCustomerId(),
				request.getAmount(), now()))
				.doOnNext(saved -> log.info("Solicitud {} autorizada por {}", saved.getRequestReference(), saved.getAmount()));
	}

	private Mono<UsageRequest> saveRejected(ProcessUsageRequest request, RejectionReason reason) {
		return usageRequestRepository.save(UsageRequest.rejected(
				request.getRequestReference(), request.getPreApprovedId(), request.getCustomerId(),
				request.getAmount(), reason, now()))
				.doOnNext(saved -> log.info("Solicitud {} rechazada: {}", saved.getRequestReference(), reason));
	}

    // Verificamos si la referencia existente y tiene datos enviados en la solicitud
	private UsageProcessingResult replay(UsageRequest existing, ProcessUsageRequest request) {
		if (!existing.hasSameContent(request.getPreApprovedId(), request.getCustomerId(), request.getAmount())) {
			log.warn("Referencia {} recibida con datos distintos a la solicitud original", request.getRequestReference());
			throw new ReferenceConflictException(request.getRequestReference());
		}
		return toResult(existing, true);
	}

	private UsageProcessingResult toResult(UsageRequest usageRequest, boolean duplicate) {
		return UsageProcessingResult.builder()
				.request(responseMapper.toResponse(usageRequest))
				.duplicate(duplicate)
				.build();
	}

	private Instant now() {

        return clock.instant().truncatedTo(ChronoUnit.MICROS);
	}

	private ProcessUsageRequest normalize(ProcessUsageRequest request) {
		return ProcessUsageRequest.builder()
				.requestReference(request.getRequestReference().trim())
				.preApprovedId(request.getPreApprovedId().trim())
				.customerId(request.getCustomerId().trim())
				.amount(request.getAmount())
				.build();
	}
}
