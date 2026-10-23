package com.ias_software.test.application.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageRequestResponse {

	private String requestReference;
	private String preApprovedId;
	private String customerId;
	private BigDecimal amount;
	private String status;
	private String reasonCode;
	private String reasonMessage;
	private Instant processedAt;
}
