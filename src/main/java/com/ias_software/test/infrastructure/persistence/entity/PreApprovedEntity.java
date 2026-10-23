package com.ias_software.test.infrastructure.persistence.entity;

import java.math.BigDecimal;

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
@Table("pre_approved")
public class PreApprovedEntity {

	@Id
	private String id;
	private String customerId;
	private String status;
	private BigDecimal availableAmount;
}
