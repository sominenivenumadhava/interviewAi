package com.interviai.backend.common.exception;

import java.util.List;
import java.util.Map;

/**
 * Exception thrown when validation fails.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
public class ValidationException extends BusinessException {

    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
    }

    public ValidationException(String field, String message) {
        super("VALIDATION_ERROR", String.format("Validation failed for field '%s': %s", field, message));
    }

    public ValidationException(Map<String, String> fieldErrors) {
        super("VALIDATION_ERROR", buildMessage(fieldErrors));
    }

    public ValidationException(List<String> errors) {
        super("VALIDATION_ERROR", String.join("; ", errors));
    }

    private static String buildMessage(Map<String, String> fieldErrors) {
        if (fieldErrors.isEmpty()) {
            return "Validation failed";
        }
        
        StringBuilder sb = new StringBuilder("Validation failed: ");
        fieldErrors.forEach((field, error) -> 
            sb.append(field).append(" - ").append(error).append("; ")
        );
        
        return sb.toString();
    }

    public static ValidationException required(String field) {
        return new ValidationException(field, "is required");
    }

    public static ValidationException invalid(String field) {
        return new ValidationException(field, "is invalid");
    }

    public static ValidationException invalidFormat(String field, String expectedFormat) {
        return new ValidationException(field, String.format("invalid format, expected: %s", expectedFormat));
    }

    public static ValidationException tooShort(String field, int minLength) {
        return new ValidationException(field, String.format("must be at least %d characters long", minLength));
    }

    public static ValidationException tooLong(String field, int maxLength) {
        return new ValidationException(field, String.format("must not exceed %d characters", maxLength));
    }

    public static ValidationException outOfRange(String field, Number min, Number max) {
        return new ValidationException(field, String.format("must be between %s and %s", min, max));
    }

    public static ValidationException alreadyExists(String field, Object value) {
        return new ValidationException(field, String.format("'%s' already exists", value));
    }
}