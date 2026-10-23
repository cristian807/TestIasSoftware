package com.ias_software.test.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.ias_software.test.domain.model.PreApproved;
import com.ias_software.test.domain.model.PreApprovedStatus;
import com.ias_software.test.domain.model.RejectionReason;

class UsageEvaluationServiceTest {

	private final UsageEvaluationService service = new UsageEvaluationService();

	private PreApproved preApproved(String customer, PreApprovedStatus status, String available) {
		return PreApproved.builder()
				.id("PRA-1001")
				.customerId(customer)
				.status(status)
				.availableAmount(new BigDecimal(available))
				.build();
	}

	@Test
	void approvesWhenAllRulesAreMet() {
		var result = service.evaluate("USR-10", new BigDecimal("600000"),
				Optional.of(preApproved("USR-10", PreApprovedStatus.ACTIVE, "1000000")));

		assertThat(result).isEmpty();
	}

	@Test
	void approvesWhenAmountEqualsAvailable() {
		var result = service.evaluate("USR-10", new BigDecimal("1000000.00"),
				Optional.of(preApproved("USR-10", PreApprovedStatus.ACTIVE, "1000000")));

		assertThat(result).isEmpty();
	}

	@Test
	void rejectsZeroAmount() {
		var result = service.evaluate("USR-10", BigDecimal.ZERO,
				Optional.of(preApproved("USR-10", PreApprovedStatus.ACTIVE, "1000000")));

		assertThat(result).contains(RejectionReason.INVALID_AMOUNT);
	}

	@Test
	void rejectsNegativeAmountEvenIfPreApprovedDoesNotExist() {
		var result = service.evaluate("USR-10", new BigDecimal("-1"), Optional.empty());

		assertThat(result).contains(RejectionReason.INVALID_AMOUNT);
	}

	@Test
	void rejectsUnknownPreApproved() {
		var result = service.evaluate("USR-10", new BigDecimal("1000"), Optional.empty());

		assertThat(result).contains(RejectionReason.PRE_APPROVED_NOT_FOUND);
	}

	@Test
	void rejectsWhenCustomerDoesNotMatch() {
		var result = service.evaluate("USR-20", new BigDecimal("1000"),
				Optional.of(preApproved("USR-10", PreApprovedStatus.ACTIVE, "1000000")));

		assertThat(result).contains(RejectionReason.CUSTOMER_MISMATCH);
	}

	@Test
	void rejectsBlockedPreApproved() {
		var result = service.evaluate("USR-10", new BigDecimal("1000"),
				Optional.of(preApproved("USR-10", PreApprovedStatus.BLOCKED, "800000")));

		assertThat(result).contains(RejectionReason.PRE_APPROVED_NOT_ENABLED);
	}

	@Test
	void rejectsWhenAmountExceedsAvailable() {
		var result = service.evaluate("USR-10", new BigDecimal("1000001"),
				Optional.of(preApproved("USR-10", PreApprovedStatus.ACTIVE, "1000000")));

		assertThat(result).contains(RejectionReason.INSUFFICIENT_FUNDS);
	}
}
