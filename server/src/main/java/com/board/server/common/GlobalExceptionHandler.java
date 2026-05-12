package com.board.server.common;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;

import software.amazon.awssdk.core.exception.SdkException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ErrorResponse> handleResponseStatusException(
		ResponseStatusException exception,
		HttpServletRequest request
	) {
		String message = exception.getReason() == null ? "요청을 처리할 수 없습니다." : exception.getReason();
		return buildResponse(exception.getStatusCode(), message, request);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(
		MethodArgumentNotValidException exception,
		HttpServletRequest request
	) {
		List<ErrorResponse.FieldErrorResponse> fieldErrors = exception.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(this::toFieldErrorResponse)
			.toList();

		String message = fieldErrors.isEmpty() ? "입력값을 확인해 주세요." : fieldErrors.get(0).message();
		ErrorResponse body = ErrorResponse.of(
			HttpStatus.BAD_REQUEST,
			HttpStatus.BAD_REQUEST.getReasonPhrase(),
			message,
			request.getRequestURI(),
			fieldErrors
		);
		return ResponseEntity.badRequest().body(body);
	}

	@ExceptionHandler({
		HttpMessageNotReadableException.class,
		MissingServletRequestParameterException.class,
		MissingServletRequestPartException.class,
		MethodArgumentTypeMismatchException.class
	})
	public ResponseEntity<ErrorResponse> handleBadRequest(Exception exception, HttpServletRequest request) {
		return buildResponse(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다.", request);
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(HttpServletRequest request) {
		return buildResponse(HttpStatus.BAD_REQUEST, "업로드할 수 있는 파일 크기를 초과했습니다.", request);
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ErrorResponse> handleAuthenticationException(HttpServletRequest request) {
		return buildResponse(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.", request);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAccessDeniedException(HttpServletRequest request) {
		return buildResponse(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.", request);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpServletRequest request) {
		return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 요청 방식입니다.", request);
	}

	@ExceptionHandler(SdkException.class)
	public ResponseEntity<ErrorResponse> handleAwsSdkException(HttpServletRequest request) {
		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "외부 저장소 처리 중 오류가 발생했습니다.", request);
	}

	@ExceptionHandler(DataAccessException.class)
	public ResponseEntity<ErrorResponse> handleDataAccessException(HttpServletRequest request) {
		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "데이터 처리 중 오류가 발생했습니다.", request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(HttpServletRequest request) {
		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.", request);
	}

	private ResponseEntity<ErrorResponse> buildResponse(HttpStatusCode statusCode, String message, HttpServletRequest request) {
		String error = statusCode instanceof HttpStatus httpStatus ? httpStatus.getReasonPhrase() : "Error";
		return ResponseEntity
			.status(statusCode)
			.body(ErrorResponse.of(statusCode, error, message, request));
	}

	private ErrorResponse.FieldErrorResponse toFieldErrorResponse(FieldError fieldError) {
		String message = fieldError.getDefaultMessage() == null ? "입력값을 확인해 주세요." : fieldError.getDefaultMessage();
		return new ErrorResponse.FieldErrorResponse(fieldError.getField(), message);
	}
}
