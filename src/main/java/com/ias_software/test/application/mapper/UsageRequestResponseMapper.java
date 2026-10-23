package com.ias_software.test.application.mapper;

import org.springframework.stereotype.Component;

import com.ias_software.test.application.dto.response.UsageRequestResponse;
import com.ias_software.test.domain.model.UsageRequest;

@Component
public class UsageRequestResponseMapper {

	public UsageRequestResponse toResponse(UsageRequest usageRequest) {
		return UsageRequestResponse.builder()
				.requestReference(usageRequest.getRequestReference())
				.preApprovedId(usageRequest.getPreApprovedId())
				.customerId(usageRequest.getCustomerId())
				.amount(usageRequest.getAmount())
				.status(usageRequest.getStatus().name())
				.reasonCode(usageRequest.getRejectionReason() != null ? usageRequest.getRejectionReason().name() : null)
				.reasonMessage(usageRequest.getRejectionReason() != null ? usageRequest.getRejectionReason().getMessage() : null)
				.processedAt(usageRequest.getProcessedAt())
				.build();
	}
}
