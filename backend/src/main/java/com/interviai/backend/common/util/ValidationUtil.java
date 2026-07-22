package com.interviai.backend.common.util;

import com.interviai.backend.common.exception.ValidationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Utility class for validation operations.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
public final class ValidationUtil {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern FILE_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+$");

    private ValidationUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Validate required field is not null or empty.
     */
    public static void requireNotNull(Object value, String fieldName) {
        if (value == null) {
            throw ValidationException.required(fieldName);
        }
    }

    /**
     * Validate required string is not null, empty, or blank.
     */
    public static void requireNotBlank(String value, String fieldName) {
        if (StringUtil.isBlank(value)) {
            throw ValidationException.required(fieldName);
        }
    }

    /**
     * Validate required collection is not null or empty.
     */
    public static void requireNotEmpty(Collection<?> collection, String fieldName) {
        if (collection == null || collection.isEmpty()) {
            throw ValidationException.required(fieldName);
        }
    }

    /**
     * Validate string length constraints.
     */
    public static void validateLength(String value, String fieldName, int minLength, int maxLength) {
        if (value == null) {
            return;
        }
        
        if (value.length() < minLength) {
            throw ValidationException.tooShort(fieldName, minLength);
        }
        
        if (value.length() > maxLength) {
            throw ValidationException.tooLong(fieldName, maxLength);
        }
    }

    /**
     * Validate number range constraints.
     */
    public static void validateRange(Number value, String fieldName, Number min, Number max) {
        if (value == null) {
            return;
        }
        
        if (value.doubleValue() < min.doubleValue() || value.doubleValue() > max.doubleValue()) {
            throw ValidationException.outOfRange(fieldName, min, max);
        }
    }

    /**
     * Validate email format.
     */
    public static void validateEmail(String email, String fieldName) {
        requireNotBlank(email, fieldName);
        
        if (!StringUtil.isValidEmail(email)) {
            throw ValidationException.invalidFormat(fieldName, "valid email address");
        }
    }

    /**
     * Validate phone number format.
     */
    public static void validatePhoneNumber(String phone, String fieldName) {
        if (StringUtil.isBlank(phone)) {
            return; // Phone is often optional
        }
        
        if (!StringUtil.isValidPhoneNumber(phone)) {
            throw ValidationException.invalidFormat(fieldName, "E.164 format (e.g., +1234567890)");
        }
    }

    /**
     * Validate password strength.
     */
    public static void validatePassword(String password, String fieldName) {
        requireNotBlank(password, fieldName);
        
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw ValidationException.invalidFormat(fieldName, 
                    "minimum 8 characters with uppercase, lowercase, number, and special character");
        }
    }

    /**
     * Validate username format.
     */
    public static void validateUsername(String username, String fieldName) {
        requireNotBlank(username, fieldName);
        
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw ValidationException.invalidFormat(fieldName, 
                    "3-20 characters, letters, numbers, and underscores only");
        }
    }

    /**
     * Validate UUID format.
     */
    public static void validateUuid(String uuid, String fieldName) {
        requireNotBlank(uuid, fieldName);
        
        try {
            UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            throw ValidationException.invalidFormat(fieldName, "valid UUID");
        }
    }

    /**
     * Validate UUID object.
     */
    public static void validateUuid(UUID uuid, String fieldName) {
        requireNotNull(uuid, fieldName);
    }

    /**
     * Validate file name.
     */
    public static void validateFileName(String fileName, String fieldName) {
        requireNotBlank(fileName, fieldName);
        
        if (!FILE_NAME_PATTERN.matcher(fileName).matches()) {
            throw ValidationException.invalidFormat(fieldName, 
                    "valid file name (letters, numbers, dots, hyphens, underscores)");
        }
    }

    /**
     * Validate date is not in the past.
     */
    public static void validateNotPast(LocalDate date, String fieldName) {
        requireNotNull(date, fieldName);
        
        if (date.isBefore(LocalDate.now())) {
            throw ValidationException.invalid(fieldName + " cannot be in the past");
        }
    }

    /**
     * Validate datetime is not in the past.
     */
    public static void validateNotPast(LocalDateTime dateTime, String fieldName) {
        requireNotNull(dateTime, fieldName);
        
        if (dateTime.isBefore(LocalDateTime.now())) {
            throw ValidationException.invalid(fieldName + " cannot be in the past");
        }
    }

    /**
     * Validate date range.
     */
    public static void validateDateRange(LocalDate startDate, LocalDate endDate, 
                                       String startFieldName, String endFieldName) {
        requireNotNull(startDate, startFieldName);
        requireNotNull(endDate, endFieldName);
        
        if (startDate.isAfter(endDate)) {
            throw new ValidationException(String.format("%s must be before %s", 
                    startFieldName, endFieldName));
        }
    }

    /**
     * Validate datetime range.
     */
    public static void validateDateTimeRange(LocalDateTime startDateTime, LocalDateTime endDateTime,
                                           String startFieldName, String endFieldName) {
        requireNotNull(startDateTime, startFieldName);
        requireNotNull(endDateTime, endFieldName);
        
        if (startDateTime.isAfter(endDateTime)) {
            throw new ValidationException(String.format("%s must be before %s", 
                    startFieldName, endFieldName));
        }
    }

    /**
     * Validate positive number.
     */
    public static void validatePositive(Number value, String fieldName) {
        requireNotNull(value, fieldName);
        
        if (value.doubleValue() <= 0) {
            throw new ValidationException(fieldName + " must be positive");
        }
    }

    /**
     * Validate non-negative number.
     */
    public static void validateNonNegative(Number value, String fieldName) {
        requireNotNull(value, fieldName);
        
        if (value.doubleValue() < 0) {
            throw new ValidationException(fieldName + " must be non-negative");
        }
    }

    /**
     * Validate custom condition with message.
     */
    public static void validate(boolean condition, String message) {
        if (!condition) {
            throw new ValidationException(message);
        }
    }

    /**
     * Validate custom condition with field name and message.
     */
    public static void validate(boolean condition, String fieldName, String message) {
        if (!condition) {
            throw ValidationException.invalid(fieldName + ": " + message);
        }
    }

    /**
     * Validate using custom predicate.
     */
    public static <T> void validate(T value, Predicate<T> predicate, String fieldName, String message) {
        requireNotNull(value, fieldName);
        
        if (!predicate.test(value)) {
            throw ValidationException.invalid(fieldName + ": " + message);
        }
    }

    /**
     * Validate enum value.
     */
    public static <T extends Enum<T>> void validateEnum(String value, Class<T> enumClass, String fieldName) {
        requireNotBlank(value, fieldName);
        
        try {
            Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw ValidationException.invalid(fieldName + " must be one of: " + 
                    java.util.Arrays.toString(enumClass.getEnumConstants()));
        }
    }

    /**
     * Validate file size.
     */
    public static void validateFileSize(long fileSize, long maxSize, String fieldName) {
        validateNonNegative(fileSize, fieldName);
        
        if (fileSize > maxSize) {
            throw new ValidationException(String.format("%s exceeds maximum size of %d bytes", 
                    fieldName, maxSize));
        }
    }

    /**
     * Validate file extension.
     */
    public static void validateFileExtension(String fileName, String[] allowedExtensions, String fieldName) {
        requireNotBlank(fileName, fieldName);
        
        String extension = getFileExtension(fileName);
        
        for (String allowed : allowedExtensions) {
            if (allowed.equalsIgnoreCase(extension)) {
                return;
            }
        }
        
        throw ValidationException.invalid(fieldName + " must have one of these extensions: " + 
                String.join(", ", allowedExtensions));
    }

    /**
     * Get file extension from filename.
     */
    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1);
    }
}