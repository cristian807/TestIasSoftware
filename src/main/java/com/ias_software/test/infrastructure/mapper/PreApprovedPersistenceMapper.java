package com.ias_software.test.infrastructure.mapper;

import org.springframework.stereotype.Component;

import com.ias_software.test.domain.model.PreApproved;
import com.ias_software.test.domain.model.PreApprovedStatus;
import com.ias_software.test.infrastructure.persistence.entity.PreApprovedEntity;

@Component
public class PreApprovedPersistenceMapper {

	public PreApproved toDomain(PreApprovedEntity entity) {
		return PreApproved.builder()
				.id(entity.getId())
				.customerId(entity.getCustomerId())
				.status(PreApprovedStatus.valueOf(entity.getStatus()))
				.availableAmount(entity.getAvailableAmount())
				.build();
	}
}
