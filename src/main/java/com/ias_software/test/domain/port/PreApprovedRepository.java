package com.ias_software.test.domain.port;

import java.math.BigDecimal;

import com.ias_software.test.domain.model.PreApproved;

import reactor.core.publisher.Mono;

public interface PreApprovedRepository {

	Mono<PreApproved> findById(String id);

	/*Descuenta el valor del cupo solo si el preaprobado esta activo y alcanza el saldo*/
	Mono<Boolean> debitIfAvailable(String id, BigDecimal amount);
}
