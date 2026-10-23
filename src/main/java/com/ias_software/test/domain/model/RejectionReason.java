package com.ias_software.test.domain.model;

public enum RejectionReason {
	INVALID_AMOUNT("El valor solicitado debe ser mayor que cero"),
	PRE_APPROVED_NOT_FOUND("El preaprobado no existe"),
	CUSTOMER_MISMATCH("El preaprobado no corresponde al cliente informado"),
	PRE_APPROVED_NOT_ENABLED("El preaprobado no se encuentra habilitado"),
	INSUFFICIENT_FUNDS("El valor solicitado supera el cupo disponible");

	private final String message;

	RejectionReason(String message) {

        this.message = message;
	}

	public String getMessage() {

        return message;
	}
}
