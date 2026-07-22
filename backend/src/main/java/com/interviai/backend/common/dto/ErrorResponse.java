package com.interviai.backend.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard error response structure for API endpoints.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private String code;
    private String message;
    private List<FieldError> details;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime timestamp = LocalDateTime.now();
    private String path;
    private String requestId;

    // Constructors
    public ErrorResponse() {}

    public ErrorResponse(String code, String message, String path) {
        this.code = code;
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<FieldError> getDetails() { return details; }
    public void setDetails(List<FieldError> details) { this.details = details; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    // Builder pattern
    public static ErrorResponseBuilder builder() {
        return new ErrorResponseBuilder();
    }

    public static class ErrorResponseBuilder {
        private ErrorResponse errorResponse = new ErrorResponse();

        public ErrorResponseBuilder code(String code) {
            errorResponse.setCode(code);
            return this;
        }

        public ErrorResponseBuilder message(String message) {
            errorResponse.setMessage(message);
            return this;
        }

        public ErrorResponseBuilder path(String path) {
            errorResponse.setPath(path);
            return this;
        }

        public ErrorResponseBuilder details(List<FieldError> details) {
            errorResponse.setDetails(details);
            return this;
        }

        public ErrorResponseBuilder requestId(String requestId) {
            errorResponse.setRequestId(requestId);
            return this;
        }

        public ErrorResponse build() {
            return errorResponse;
        }
    }

    /**
     * Field level error details.
     */
    public static class FieldError {
        private String field;
        private Object rejectedValue;
        private String message;

        // Constructors
        public FieldError() {}

        public FieldError(String field, Object rejectedValue, String message) {
            this.field = field;
            this.rejectedValue = rejectedValue;
            this.message = message;
        }

        // Getters and Setters
        public String getField() { return field; }
        public void setField(String field) { this.field = field; }

        public Object getRejectedValue() { return rejectedValue; }
        public void setRejectedValue(Object rejectedValue) { this.rejectedValue = rejectedValue; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        // Builder pattern
        public static FieldErrorBuilder builder() {
            return new FieldErrorBuilder();
        }

        public static class FieldErrorBuilder {
            private FieldError fieldError = new FieldError();

            public FieldErrorBuilder field(String field) {
                fieldError.setField(field);
                return this;
            }

            public FieldErrorBuilder rejectedValue(Object rejectedValue) {
                fieldError.setRejectedValue(rejectedValue);
                return this;
            }

            public FieldErrorBuilder message(String message) {
                fieldError.setMessage(message);
                return this;
            }

            public FieldError build() {
                return fieldError;
            }
        }
    }

    /**
     * Create error response for validation errors.
     */
    public static ErrorResponse validationError(List<FieldError> fieldErrors, String path) {
        return ErrorResponse.builder()
                .code("VALIDATION_ERROR")
                .message("Invalid input data")
                .details(fieldErrors)
                .path(path)
                .build();
    }

    /**
     * Create error response for business errors.
     */
    public static ErrorResponse businessError(String code, String message, String path) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .path(path)
                .build();
    }

    /**
     * Create error response for system errors.
     */
    public static ErrorResponse systemError(String message, String path) {
        return ErrorResponse.builder()
                .code("SYSTEM_ERROR")
                .message(message)
                .path(path)
                .build();
    }

    /**
     * Create error response for authentication errors.
     */
    public static ErrorResponse authenticationError(String message, String path) {
        return ErrorResponse.builder()
                .code("AUTHENTICATION_ERROR")
                .message(message)
                .path(path)
                .build();
    }

    /**
     * Create error response for authorization errors.
     */
    public static ErrorResponse authorizationError(String message, String path) {
        return ErrorResponse.builder()
                .code("AUTHORIZATION_ERROR")
                .message(message)
                .path(path)
                .build();
    }
}