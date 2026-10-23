package com.ias_software.test.infrastructure.mapper;

import org.springframework.stereotype.Component;

import com.ias_software.test.domain.model.RejectionReason;
import com.ias_software.test.domain.model.UsageRequest;
import com.ias_software.test.domain.model.UsageStatus;
import com.ias_software.test.infrastructure.persistence.entity.UsageRequestEntity;

@Component
public class UsageRequestPersistenceMapper {

	public UsageRequestEntity toEntity(UsageRequest usageRequest) {
		return UsageRequestEntity.builder()
				.requestReference(usageRequest.getRequestReference())
				.preApprovedId(usageRequest.getPreApprovedId())
				.customerId(usageRequest.getCustomerId())
				.amount(usageRequest.getAmount())
				.status(usageRequest.getStatus().name())
				.reasonCode(usageRequest.getRejectionReason() != null ? usageRequest.getRejectionReason().name() : null)
				.processedAt(usageRequest.getProcessedAt())
				.build();
	}

	public UsageRequest toDomain(UsageRequestEntity entity) {
		return UsageRequest.builder()
				.requestReference(entity.getRequestReference())
				.preApprovedId(entity.getPreApprovedId())
				.customerId(entity.getCustomerId())
				.amount(entity.getAmount())
				.status(UsageStatus.valueOf(entity.getStatus()))
				.rejectionReason(entity.getReasonCode() != null ? RejectionReason.valueOf(entity.getReasonCode()) : null)
				.processedAt(entity.getProcessedAt())
				.build();
	}
}
