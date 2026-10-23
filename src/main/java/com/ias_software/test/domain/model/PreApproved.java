package com.ias_software.test.domain.model;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PreApproved {

	private final String id;
	private final String customerId;
	private final PreApprovedStatus status;
	private final BigDecimal availableAmount;

	public boolean belongsTo(String customer) {

        return customerId.equals(customer);
	}

	public boolean isEnabled() {

        return status == PreApprovedStatus.ACTIVE;
	}

	public boolean covers(BigDecimal amount) {

        return availableAmount.compareTo(amount) >= 0;
	}
}
