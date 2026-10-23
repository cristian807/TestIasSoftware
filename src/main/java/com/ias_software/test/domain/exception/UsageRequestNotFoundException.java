package com.ias_software.test.domain.exception;

public class UsageRequestNotFoundException extends RuntimeException {

	public UsageRequestNotFoundException(String requestReference) {
		super("No existe una solicitud con la referencia " + requestReference);
	}
}
