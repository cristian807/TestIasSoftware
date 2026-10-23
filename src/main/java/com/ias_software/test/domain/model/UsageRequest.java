package com.ias_software.test.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UsageRequest {

	private final String requestReference;
	private final String preApprovedId;
	private final String customerId;
	private final BigDecimal amount;
	private final UsageStatus status;
	private final RejectionReason rejectionReason;
	private final Instant processedAt;

	public static UsageRequest authorized(
            String requestReference,
            String preApprovedId,
            String customerId,
			BigDecimal amount,
            Instant processedAt
    ) {
		return UsageRequest.builder()
				.requestReference(requestReference)
				.preApprovedId(preApprovedId)
				.customerId(customerId)
				.amount(amount)
				.status(UsageStatus.AUTHORIZED)
				.processedAt(processedAt)
				.build();
	}

	public static UsageRequest rejected(
            String requestReference,
            String preApprovedId,
            String customerId,
			BigDecimal amount,
            RejectionReason reason,
            Instant processedAt
    ) {
		return UsageRequest.builder()
				.requestReference(requestReference)
				.preApprovedId(preApprovedId)
				.customerId(customerId)
				.amount(amount)
				.status(UsageStatus.REJECTED)
				.rejectionReason(reason)
				.processedAt(processedAt)
				.build();
	}

	public boolean isAuthorized() {

        return status == UsageStatus.AUTHORIZED;
	}

	public boolean hasSameContent(
            String preApproved,
            String customer,
            BigDecimal requestedAmount
    ) {
		return preApprovedId.equals(preApproved)
				&& customerId.equals(customer)
				&& amount.compareTo(requestedAmount) == 0;
	}
}
