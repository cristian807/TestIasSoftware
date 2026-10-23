package com.ias_software.test.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

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
@Table("usage_requests")
public class UsageRequestEntity {

	@Id
	private Long id;
	private String requestReference;
	private String preApprovedId;
	private String customerId;
	private BigDecimal amount;
	private String status;
	private String reasonCode;
	private Instant processedAt;
}
