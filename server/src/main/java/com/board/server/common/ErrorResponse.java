package com.board.server.common;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.http.HttpStatusCode;

import jakarta.servlet.http.HttpServletRequest;

public record ErrorResponse(
	OffsetDateTime timestamp,
	int status,
	String error,
	String message,
	String path,
	List<FieldErrorResponse> fieldErrors
) {

	public static ErrorResponse of(HttpStatusCode statusCode, String error, String message, HttpServletRequest request) {
		return of(statusCode, error, message, request.getRequestURI(), List.of());
	}

	public static ErrorResponse of(
		HttpStatusCode statusCode,
		String error,
		String message,
		String path,
		List<FieldErrorResponse> fieldErrors
	) {
		return new ErrorResponse(
			OffsetDateTime.now(),
			statusCode.value(),
			error,
			message,
			path,
			fieldErrors
		);
	}

	public record FieldErrorResponse(
		String field,
		String message
	) {
	}
}
