package com.ias_software.test.presentation.exception;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;

import com.ias_software.test.domain.exception.ReferenceConflictException;
import com.ias_software.test.domain.exception.UsageRequestNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(UsageRequestNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleNotFound(UsageRequestNotFoundException ex, ServerWebExchange exchange) {
		return build(HttpStatus.NOT_FOUND, ex.getMessage(), exchange, null);
	}

	@ExceptionHandler(ReferenceConflictException.class)
	public ResponseEntity<ApiErrorResponse> handleConflict(ReferenceConflictException ex, ServerWebExchange exchange) {
		return build(HttpStatus.CONFLICT, ex.getMessage(), exchange, null);
	}

	@ExceptionHandler(WebExchangeBindException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(WebExchangeBindException ex, ServerWebExchange exchange) {
		List<String> details = ex.getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.sorted()
				.toList();
		return build(HttpStatus.BAD_REQUEST, "La solicitud contiene datos invalidos", exchange, details);
	}

	@ExceptionHandler(ServerWebInputException.class)
	public ResponseEntity<ApiErrorResponse> handleUnreadableInput(ServerWebInputException ex, ServerWebExchange exchange) {
		return build(HttpStatus.BAD_REQUEST, "El cuerpo o los parametros de la peticion no son validos", exchange, null);
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException ex, ServerWebExchange exchange) {
		HttpStatusCode status = ex.getStatusCode();
		String message = ex.getReason() != null ? ex.getReason() : "No fue posible atender la peticion";
		return build(status, message, exchange, null);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, ServerWebExchange exchange) {
		log.error("Error no controlado en {} {}", exchange.getRequest().getMethod(), exchange.getRequest().getPath(), ex);
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno, intente nuevamente", exchange, null);
	}

	private ResponseEntity<ApiErrorResponse> build(HttpStatusCode status, String message, ServerWebExchange exchange,
			List<String> details) {
		String reason = status instanceof HttpStatus httpStatus ? httpStatus.getReasonPhrase() : status.toString();
		ApiErrorResponse body = ApiErrorResponse.builder()
				.timestamp(Instant.now())
				.status(status.value())
				.error(reason)
				.message(message)
				.path(exchange.getRequest().getPath().value())
				.details(details)
				.build();
		return ResponseEntity.status(status).body(body);
	}
}
