package com.ias_software.test.domain.service;

import java.math.BigDecimal;
import java.util.Optional;

import com.ias_software.test.domain.model.PreApproved;
import com.ias_software.test.domain.model.RejectionReason;

public class UsageEvaluationService {

	public Optional<RejectionReason> evaluate(String customerId, BigDecimal amount, Optional<PreApproved> preApproved) {
		if (amount.signum() <= 0) {
			return Optional.of(RejectionReason.INVALID_AMOUNT);
		}
		if (preApproved.isEmpty()) {
			return Optional.of(RejectionReason.PRE_APPROVED_NOT_FOUND);
		}
		PreApproved found = preApproved.get();
		if (!found.belongsTo(customerId)) {
			return Optional.of(RejectionReason.CUSTOMER_MISMATCH);
		}
		if (!found.isEnabled()) {
			return Optional.of(RejectionReason.PRE_APPROVED_NOT_ENABLED);
		}
		if (!found.covers(amount)) {
			return Optional.of(RejectionReason.INSUFFICIENT_FUNDS);
		}
		return Optional.empty();
	}
}
