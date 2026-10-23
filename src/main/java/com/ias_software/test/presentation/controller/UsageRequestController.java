package com.ias_software.test.presentation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ias_software.test.application.dto.request.ProcessUsageRequest;
import com.ias_software.test.application.dto.response.UsageRequestResponse;
import com.ias_software.test.application.service.UsageRequestService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/usage-requests")
@RequiredArgsConstructor
public class UsageRequestController {

	private final UsageRequestService usageRequestService;

	@PostMapping
	public Mono<ResponseEntity<UsageRequestResponse>> process(
            @Valid
            @RequestBody ProcessUsageRequest request
    ) {
		return usageRequestService.process(request)
				.map(result -> ResponseEntity
                .status(result.isDuplicate() ? HttpStatus.OK : HttpStatus.CREATED)
				.body(result.getRequest()));
	}

	@GetMapping("/{requestReference}")
	public Mono<UsageRequestResponse> findByReference(@PathVariable String requestReference) {
		return usageRequestService.findByReference(requestReference);
	}

	@GetMapping
	public Flux<UsageRequestResponse> findRecent(
            @RequestParam(required = false) Integer limit
    ) {
		return usageRequestService.findRecent(limit);
	}
}
