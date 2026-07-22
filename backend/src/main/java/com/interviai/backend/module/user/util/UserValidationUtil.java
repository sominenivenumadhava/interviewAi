package com.interviai.backend.module.user.util;

import com.interviai.backend.common.exception.ValidationException;
import com.interviai.backend.common.util.StringUtil;
import com.interviai.backend.common.util.ValidationUtil;
import com.interviai.backend.module.user.dto.request.ChangePasswordRequest;
import com.interviai.backend.module.user.dto.request.RegisterRequest;
import com.interviai.backend.module.user.dto.request.UpdateProfileRequest;
import com.interviai.backend.module.user.entity.User;

import java.util.regex.Pattern;

/**
 * Utility class for user-specific validation operations.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
public final class UserValidationUtil {

    // Timezone pattern (basic validation)
    private static final Pattern TIMEZONE_PATTERN = Pattern.compile(
            "^[A-Za-z]+/[A-Za-z_]+$|^UTC$|^GMT[+-]\\d{1,2}$"
    );

    // Language code pattern (ISO 639-1)
    private static final Pattern LANGUAGE_PATTERN = Pattern.compile("^[a-z]{2}$");

    // Name pattern (letters, spaces, apostrophes, hyphens)
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s'-]+$");

    private UserValidationUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Validate user registration request.
     */
    public static void validateRegistrationRequest(RegisterRequest request) {
        if (request == null) {
            throw new ValidationException("Registration request is required");
        }

        // Required fields
        ValidationUtil.requireNotBlank(request.getEmail(), "email");
        ValidationUtil.requireNotBlank(request.getPassword(), "password");
        ValidationUtil.requireNotBlank(request.getConfirmPassword(), "confirmPassword");
        ValidationUtil.requireNotBlank(request.getFirstName(), "firstName");
        ValidationUtil.requireNotBlank(request.getLastName(), "lastName");

        // Email validation
        ValidationUtil.validateEmail(request.getEmail(), "email");

        // Password validation
        ValidationUtil.validatePassword(request.getPassword(), "password");
        
        // Password confirmation
        if (!request.passwordsMatch()) {
            throw new ValidationException("Password confirmation does not match");
        }

        // Name validation
        validateName(request.getFirstName(), "firstName");
        validateName(request.getLastName(), "lastName");

        // Optional username validation
        if (StringUtil.isNotBlank(request.getUsername())) {
            ValidationUtil.validateUsername(request.getUsername(), "username");
        }

        // Optional phone validation
        if (StringUtil.isNotBlank(request.getPhone())) {
            ValidationUtil.validatePhoneNumber(request.getPhone(), "phone");
        }

        // Optional timezone validation
        if (StringUtil.isNotBlank(request.getTimezone())) {
            validateTimezone(request.getTimezone(), "timezone");
        }

        // Optional language validation
        if (StringUtil.isNotBlank(request.getLanguage())) {
            validateLanguage(request.getLanguage(), "language");
        }

        // Terms acceptance validation
        if (!request.hasAcceptedRequiredTerms()) {
            throw new ValidationException("You must accept the terms and conditions and privacy policy");
        }
    }

    /**
     * Validate user profile update request.
     */
    public static void validateUpdateProfileRequest(UpdateProfileRequest request) {
        if (request == null) {
            throw new ValidationException("Profile update request is required");
        }

        // Optional username validation
        if (StringUtil.isNotBlank(request.getUsername())) {
            ValidationUtil.validateUsername(request.getUsername(), "username");
        }

        // Optional name validation
        if (StringUtil.isNotBlank(request.getFirstName())) {
            validateName(request.getFirstName(), "firstName");
        }

        if (StringUtil.isNotBlank(request.getLastName())) {
            validateName(request.getLastName(), "lastName");
        }

        // Optional phone validation
        if (StringUtil.isNotBlank(request.getPhone())) {
            ValidationUtil.validatePhoneNumber(request.getPhone(), "phone");
        }

        // Optional bio validation
        if (StringUtil.isNotBlank(request.getBio())) {
            validateBio(request.getBio());
        }

        // Optional timezone validation
        if (StringUtil.isNotBlank(request.getTimezone())) {
            validateTimezone(request.getTimezone(), "timezone");
        }

        // Optional language validation
        if (StringUtil.isNotBlank(request.getLanguage())) {
            validateLanguage(request.getLanguage(), "language");
        }
    }

    /**
     * Validate change password request.
     */
    public static void validateChangePasswordRequest(ChangePasswordRequest request) {
        if (request == null) {
            throw new ValidationException("Change password request is required");
        }

        ValidationUtil.requireNotBlank(request.getCurrentPassword(), "currentPassword");
        ValidationUtil.requireNotBlank(request.getNewPassword(), "newPassword");
        ValidationUtil.requireNotBlank(request.getConfirmNewPassword(), "confirmNewPassword");

        // Validate new password strength
        ValidationUtil.validatePassword(request.getNewPassword(), "newPassword");

        // Check password confirmation
        if (!request.passwordsMatch()) {
            throw new ValidationException("Password confirmation does not match");
        }

        // Check if new password is different
        if (!request.isNewPasswordDifferent()) {
            throw new ValidationException("New password must be different from current password");
        }
    }

    /**
     * Validate name format.
     */
    public static void validateName(String name, String fieldName) {
        ValidationUtil.requireNotBlank(name, fieldName);
        ValidationUtil.validateLength(name, fieldName, 1, 100);

        if (!NAME_PATTERN.matcher(name).matches()) {
            throw ValidationException.invalidFormat(fieldName, 
                    "letters, spaces, apostrophes, and hyphens only");
        }
    }

    /**
     * Validate bio content.
     */
    public static void validateBio(String bio) {
        if (StringUtil.isBlank(bio)) {
            return;
        }

        ValidationUtil.validateLength(bio, "bio", 0, 500);

        // Check for inappropriate content patterns (basic check)
        if (containsInappropriateContent(bio)) {
            throw new ValidationException("Bio contains inappropriate content");
        }
    }

    /**
     * Validate timezone format.
     */
    public static void validateTimezone(String timezone, String fieldName) {
        ValidationUtil.requireNotBlank(timezone, fieldName);
        ValidationUtil.validateLength(timezone, fieldName, 3, 50);

        if (!TIMEZONE_PATTERN.matcher(timezone).matches()) {
            throw ValidationException.invalidFormat(fieldName, 
                    "valid timezone (e.g., America/New_York, UTC, GMT+5)");
        }
    }

    /**
     * Validate language code format.
     */
    public static void validateLanguage(String language, String fieldName) {
        ValidationUtil.requireNotBlank(language, fieldName);

        if (!LANGUAGE_PATTERN.matcher(language).matches()) {
            throw ValidationException.invalidFormat(fieldName, 
                    "valid ISO 639-1 language code (e.g., en, es, fr)");
        }
    }

    /**
     * Validate user role.
     */
    public static void validateUserRole(User.UserRole role) {
        ValidationUtil.requireNotNull(role, "role");
    }

    /**
     * Validate profile picture URL.
     */
    public static void validateProfilePictureUrl(String url) {
        if (StringUtil.isBlank(url)) {
            return;
        }

        ValidationUtil.validateLength(url, "profilePictureUrl", 0, 500);

        if (!isValidUrl(url)) {
            throw ValidationException.invalidFormat("profilePictureUrl", "valid URL");
        }

        // Basic security check - only allow https URLs
        if (!url.toLowerCase().startsWith("https://")) {
            throw new ValidationException("Profile picture URL must use HTTPS");
        }
    }

    /**
     * Validate user account state consistency.
     */
    public static void validateUserAccountState(User user) {
        if (user == null) {
            throw new ValidationException("User is required");
        }

        // Check for logical inconsistencies
        if (user.getIsDeleted() && user.getIsActive()) {
            throw new ValidationException("User cannot be both deleted and active");
        }

        if (user.isAccountLocked() && user.getAccountLockedUntil() == null) {
            throw new ValidationException("Locked account must have lock expiry time");
        }

        if (user.getEmailVerificationToken() != null && user.getEmailVerificationExpiresAt() == null) {
            throw new ValidationException("Email verification token must have expiry time");
        }

        if (user.getPasswordResetToken() != null && user.getPasswordResetExpiresAt() == null) {
            throw new ValidationException("Password reset token must have expiry time");
        }
    }

    /**
     * Check if text contains inappropriate content (basic implementation).
     */
    private static boolean containsInappropriateContent(String text) {
        if (StringUtil.isBlank(text)) {
            return false;
        }

        String lowerText = text.toLowerCase();
        
        // Basic profanity check (expand this list as needed)
        String[] inappropriateWords = {
            "spam", "scam", "fraud", "hack", "virus"
        };

        for (String word : inappropriateWords) {
            if (lowerText.contains(word)) {
                return true;
            }
        }

        // Check for suspicious patterns
        if (lowerText.contains("http://") || lowerText.contains("www.")) {
            return true; // No URLs in bio
        }

        return false;
    }

    /**
     * Basic URL validation.
     */
    private static boolean isValidUrl(String url) {
        if (StringUtil.isBlank(url)) {
            return false;
        }
        
        // Basic URL pattern check
        return url.matches("^https?://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(/.*)?$");
    }

    /**
     * Validate email verification token format.
     */
    public static void validateEmailVerificationToken(String token) {
        ValidationUtil.requireNotBlank(token, "emailVerificationToken");
        
        try {
            java.util.UUID.fromString(token);
        } catch (IllegalArgumentException e) {
            throw ValidationException.invalidFormat("emailVerificationToken", "valid UUID");
        }
    }

    /**
     * Validate password reset token format.
     */
    public static void validatePasswordResetToken(String token) {
        ValidationUtil.requireNotBlank(token, "passwordResetToken");
        
        try {
            java.util.UUID.fromString(token);
        } catch (IllegalArgumentException e) {
            throw ValidationException.invalidFormat("passwordResetToken", "valid UUID");
        }
    }

    /**
     * Validate user search query.
     */
    public static void validateSearchQuery(String query) {
        ValidationUtil.requireNotBlank(query, "search query");
        ValidationUtil.validateLength(query, "search query", 2, 100);

        // Basic security check against SQL injection patterns
        String lowerQuery = query.toLowerCase();
        String[] suspiciousPatterns = {
            "select", "insert", "update", "delete", "drop", "union", 
            "script", "<", ">", "javascript:", "onload", "onerror"
        };

        for (String pattern : suspiciousPatterns) {
            if (lowerQuery.contains(pattern)) {
                throw new ValidationException("Search query contains invalid characters");
            }
        }
    }
}