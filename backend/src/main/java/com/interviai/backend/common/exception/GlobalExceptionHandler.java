package com.interviai.backend.common.exception;

import com.interviai.backend.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Global exception handler for all REST controllers.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        log.warn("Business exception [{}]: {} - Path: {}", 
                requestId, ex.getMessage(), request.getRequestURI());
        
        ErrorResponse errorResponse = ErrorResponse.businessError(
                ex.getErrorCode(), ex.getMessage(), request.getRequestURI());
        errorResponse.setRequestId(requestId);
        
        HttpStatus status = determineHttpStatus(ex);
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        log.warn("Validation exception [{}]: {} - Path: {}", 
                requestId, ex.getMessage(), request.getRequestURI());
        
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::mapFieldError)
                .collect(Collectors.toList());
        
        ErrorResponse errorResponse = ErrorResponse.validationError(fieldErrors, request.getRequestURI());
        errorResponse.setRequestId(requestId);
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        log.warn("Constraint violation exception [{}]: {} - Path: {}", 
                requestId, ex.getMessage(), request.getRequestURI());
        
        List<ErrorResponse.FieldError> fieldErrors = ex.getConstraintViolations()
                .stream()
                .map(this::mapConstraintViolation)
                .collect(Collectors.toList());
        
        ErrorResponse errorResponse = ErrorResponse.validationError(fieldErrors, request.getRequestURI());
        errorResponse.setRequestId(requestId);
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        log.warn("Authentication exception [{}]: {} - Path: {}", 
                requestId, ex.getMessage(), request.getRequestURI());
        
        ErrorResponse errorResponse = ErrorResponse.authenticationError(
                ex.getMessage(), request.getRequestURI());
        errorResponse.setRequestId(requestId);
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        log.warn("Access denied exception [{}]: {} - Path: {}", 
                requestId, ex.getMessage(), request.getRequestURI());
        
        ErrorResponse errorResponse = ErrorResponse.authorizationError(
                "Access denied", request.getRequestURI());
        errorResponse.setRequestId(requestId);
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        log.error("Data integrity violation [{}]: {} - Path: {}", 
                requestId, ex.getMessage(), request.getRequestURI());
        
        String message = "Data integrity constraint violation";
        if (ex.getMessage() != null) {
            if (ex.getMessage().contains("unique")) {
                message = "Resource already exists";
            } else if (ex.getMessage().contains("foreign key")) {
                message = "Referenced resource not found";
            }
        }
        
        ErrorResponse errorResponse = ErrorResponse.businessError(
                "DATA_INTEGRITY_ERROR", message, request.getRequestURI());
        errorResponse.setRequestId(requestId);
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        log.warn("Message not readable exception [{}]: {} - Path: {}", 
                requestId, ex.getMessage(), request.getRequestURI());
        
        ErrorResponse errorResponse = ErrorResponse.businessError(
                "INVALID_JSON", "Invalid JSON format", request.getRequestURI());
        errorResponse.setRequestId(requestId);
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        log.warn("Method argument type mismatch [{}]: {} - Path: {}", 
                requestId, ex.getMessage(), request.getRequestURI());
        
        String message = String.format("Invalid value '%s' for parameter '%s'", 
                ex.getValue(), ex.getName());
        
        ErrorResponse errorResponse = ErrorResponse.businessError(
                "INVALID_PARAMETER", message, request.getRequestURI());
        errorResponse.setRequestId(requestId);
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        log.warn("Missing request parameter [{}]: {} - Path: {}", 
                requestId, ex.getMessage(), request.getRequestURI());
        
        String message = String.format("Required parameter '%s' is missing", ex.getParameterName());
        
        ErrorResponse errorResponse = ErrorResponse.businessError(
                "MISSING_PARAMETER", message, request.getRequestURI());
        errorResponse.setRequestId(requestId);
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        log.warn("Method not supported [{}]: {} - Path: {}", 
                requestId, ex.getMessage(), request.getRequestURI());
        
        String message = String.format("HTTP method '%s' is not supported for this endpoint", 
                ex.getMethod());
        
        ErrorResponse errorResponse = ErrorResponse.businessError(
                "METHOD_NOT_SUPPORTED", message, request.getRequestURI());
        errorResponse.setRequestId(requestId);
        
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        log.warn("File size exceeded [{}]: {} - Path: {}", 
                requestId, ex.getMessage(), request.getRequestURI());
        
        ErrorResponse errorResponse = ErrorResponse.businessError(
                "FILE_SIZE_EXCEEDED", "File size exceeds maximum allowed limit", 
                request.getRequestURI());
        errorResponse.setRequestId(requestId);
        
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        String requestId = generateRequestId();
        log.error("Unexpected exception [{}]: {} - Path: {}", 
                requestId, ex.getMessage(), request.getRequestURI(), ex);
        
        ErrorResponse errorResponse = ErrorResponse.systemError(
                "An unexpected error occurred", request.getRequestURI());
        errorResponse.setRequestId(requestId);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private ErrorResponse.FieldError mapFieldError(FieldError fieldError) {
        return ErrorResponse.FieldError.builder()
                .field(fieldError.getField())
                .rejectedValue(fieldError.getRejectedValue())
                .message(fieldError.getDefaultMessage())
                .build();
    }

    private ErrorResponse.FieldError mapConstraintViolation(ConstraintViolation<?> violation) {
        return ErrorResponse.FieldError.builder()
                .field(violation.getPropertyPath().toString())
                .rejectedValue(violation.getInvalidValue())
                .message(violation.getMessage())
                .build();
    }

    private HttpStatus determineHttpStatus(BusinessException ex) {
        return switch (ex.getErrorCode()) {
            case "RESOURCE_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "VALIDATION_ERROR" -> HttpStatus.BAD_REQUEST;
            case "UNAUTHORIZED" -> HttpStatus.UNAUTHORIZED;
            case "FORBIDDEN" -> HttpStatus.FORBIDDEN;
            case "EXTERNAL_SERVICE_ERROR" -> HttpStatus.BAD_GATEWAY;
            default -> HttpStatus.BAD_REQUEST;
        };
    }

    private String generateRequestId() {
        return UUID.randomUUID().toString();
    }
}