package com.ias_software.test.domain.exception;

public class DuplicateRequestReferenceException extends RuntimeException {

	public DuplicateRequestReferenceException(String requestReference, Throwable cause) {
		super("La referencia ya fue registrada: " + requestReference, cause);
	}
}
