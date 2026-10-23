package com.ias_software.test.domain.exception;

public class ReferenceConflictException extends RuntimeException {

	public ReferenceConflictException(String requestReference) {
		super("La referencia " + requestReference
				+ " ya fue procesada con informacion diferente; se conserva la solicitud original");
	}
}
