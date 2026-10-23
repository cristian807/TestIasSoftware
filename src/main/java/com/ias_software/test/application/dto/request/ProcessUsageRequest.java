package com.ias_software.test.application.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class ProcessUsageRequest {

	@NotBlank(message = "requestReference es obligatorio")
	@Size(max = 64, message = "requestReference no puede superar 64 caracteres")
	private String requestReference;

	@NotBlank(message = "preApprovedId es obligatorio")
	@Size(max = 32, message = "preApprovedId no puede superar 32 caracteres")
	private String preApprovedId;

	@NotBlank(message = "customerId es obligatorio")
	@Size(max = 32, message = "customerId no puede superar 32 caracteres")
	private String customerId;

	@NotNull(message = "amount es obligatorio")
	@Digits(integer = 16, fraction = 2, message = "amount admite maximo 16 enteros y 2 decimales")
	private BigDecimal amount;
}
