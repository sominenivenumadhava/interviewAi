package com.interviai.backend.common.util;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Utility class for string operations.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
public final class StringUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[1-9]\\d{1,14}$"
    );

    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9]*$");
    private static final Pattern ALPHABETIC_PATTERN = Pattern.compile("^[a-zA-Z]*$");
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^[0-9]*$");

    private StringUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Check if string is null or empty.
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * Check if string is not null and not empty.
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * Check if string is null, empty, or contains only whitespace.
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Check if string is not null, not empty, and contains non-whitespace characters.
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * Get string or default value if null/empty.
     */
    public static String defaultIfEmpty(String str, String defaultValue) {
        return isEmpty(str) ? defaultValue : str;
    }

    /**
     * Get string or default value if null/blank.
     */
    public static String defaultIfBlank(String str, String defaultValue) {
        return isBlank(str) ? defaultValue : str;
    }

    /**
     * Trim string and return null if empty.
     */
    public static String trimToNull(String str) {
        if (str == null) {
            return null;
        }
        String trimmed = str.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Trim string and return empty string if null.
     */
    public static String trimToEmpty(String str) {
        return str == null ? "" : str.trim();
    }

    /**
     * Capitalize first letter of string.
     */
    public static String capitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    /**
     * Convert string to title case.
     */
    public static String toTitleCase(String str) {
        if (isEmpty(str)) {
            return str;
        }
        
        String[] words = str.toLowerCase().split("\\s+");
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                result.append(" ");
            }
            result.append(capitalize(words[i]));
        }
        
        return result.toString();
    }

    /**
     * Convert string to camelCase.
     */
    public static String toCamelCase(String str) {
        if (isEmpty(str)) {
            return str;
        }
        
        String[] words = str.toLowerCase().split("[\\s_-]+");
        StringBuilder result = new StringBuilder(words[0]);
        
        for (int i = 1; i < words.length; i++) {
            result.append(capitalize(words[i]));
        }
        
        return result.toString();
    }

    /**
     * Convert string to snake_case.
     */
    public static String toSnakeCase(String str) {
        if (isEmpty(str)) {
            return str;
        }
        
        return str.replaceAll("([a-z])([A-Z])", "$1_$2")
                .replaceAll("\\s+", "_")
                .toLowerCase();
    }

    /**
     * Convert string to kebab-case.
     */
    public static String toKebabCase(String str) {
        if (isEmpty(str)) {
            return str;
        }
        
        return str.replaceAll("([a-z])([A-Z])", "$1-$2")
                .replaceAll("[\\s_]+", "-")
                .toLowerCase();
    }

    /**
     * Validate email format.
     */
    public static boolean isValidEmail(String email) {
        return isNotEmpty(email) && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate phone number format (E.164).
     */
    public static boolean isValidPhoneNumber(String phone) {
        return isNotEmpty(phone) && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Check if string contains only alphanumeric characters.
     */
    public static boolean isAlphanumeric(String str) {
        return isNotEmpty(str) && ALPHANUMERIC_PATTERN.matcher(str).matches();
    }

    /**
     * Check if string contains only alphabetic characters.
     */
    public static boolean isAlphabetic(String str) {
        return isNotEmpty(str) && ALPHABETIC_PATTERN.matcher(str).matches();
    }

    /**
     * Check if string contains only numeric characters.
     */
    public static boolean isNumeric(String str) {
        return isNotEmpty(str) && NUMERIC_PATTERN.matcher(str).matches();
    }

    /**
     * Generate random UUID string.
     */
    public static String generateUuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * Mask sensitive information (e.g., email, phone).
     */
    public static String maskEmail(String email) {
        if (isEmpty(email) || !email.contains("@")) {
            return email;
        }
        
        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];
        
        if (username.length() <= 3) {
            return "*".repeat(username.length()) + "@" + domain;
        }
        
        return username.substring(0, 2) + "*".repeat(username.length() - 2) + "@" + domain;
    }

    /**
     * Mask phone number.
     */
    public static String maskPhoneNumber(String phone) {
        if (isEmpty(phone) || phone.length() < 4) {
            return phone;
        }
        
        return "*".repeat(phone.length() - 4) + phone.substring(phone.length() - 4);
    }

    /**
     * Truncate string to specified length with ellipsis.
     */
    public static String truncate(String str, int maxLength) {
        if (isEmpty(str) || str.length() <= maxLength) {
            return str;
        }
        
        return str.substring(0, maxLength - 3) + "...";
    }

    /**
     * Remove accents and diacritics from string.
     */
    public static String removeAccents(String str) {
        if (isEmpty(str)) {
            return str;
        }
        
        return Normalizer.normalize(str, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    /**
     * Generate slug from string (URL-friendly).
     */
    public static String toSlug(String str) {
        if (isEmpty(str)) {
            return str;
        }
        
        return removeAccents(str)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    /**
     * Join strings with delimiter.
     */
    public static String join(String delimiter, String... strings) {
        return String.join(delimiter, strings);
    }

    /**
     * Join non-empty strings with delimiter.
     */
    public static String joinNonEmpty(String delimiter, String... strings) {
        return Arrays.stream(strings)
                .filter(StringUtil::isNotEmpty)
                .reduce((a, b) -> a + delimiter + b)
                .orElse("");
    }

    /**
     * Split string and trim each part.
     */
    public static List<String> splitAndTrim(String str, String delimiter) {
        if (isEmpty(str)) {
            return List.of();
        }
        
        return Arrays.stream(str.split(delimiter))
                .map(String::trim)
                .filter(StringUtil::isNotEmpty)
                .toList();
    }

    /**
     * Count occurrences of substring in string.
     */
    public static int countOccurrences(String str, String substring) {
        if (isEmpty(str) || isEmpty(substring)) {
            return 0;
        }
        
        int count = 0;
        int index = 0;
        
        while ((index = str.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        
        return count;
    }

    /**
     * Check if string starts with any of the given prefixes.
     */
    public static boolean startsWithAny(String str, String... prefixes) {
        if (isEmpty(str) || prefixes == null) {
            return false;
        }
        
        return Arrays.stream(prefixes)
                .anyMatch(str::startsWith);
    }

    /**
     * Check if string ends with any of the given suffixes.
     */
    public static boolean endsWithAny(String str, String... suffixes) {
        if (isEmpty(str) || suffixes == null) {
            return false;
        }
        
        return Arrays.stream(suffixes)
                .anyMatch(str::endsWith);
    }
}