package com.interviai.backend.module.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for user registration.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User registration request payload")
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Schema(description = "User email address", example = "user@example.com", required = true)
    private String email;

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    @Schema(description = "Username (optional)", example = "john_doe")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
             message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character")
    @Schema(description = "User password", example = "SecurePass123!", required = true)
    private String password;

    @NotBlank(message = "Password confirmation is required")
    @Schema(description = "Password confirmation", example = "SecurePass123!", required = true)
    private String confirmPassword;

    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "First name can only contain letters, spaces, apostrophes, and hyphens")
    @Schema(description = "User first name", example = "John", required = true)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Last name can only contain letters, spaces, apostrophes, and hyphens")
    @Schema(description = "User last name", example = "Doe", required = true)
    private String lastName;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format (E.164)")
    @Schema(description = "Phone number in E.164 format", example = "+1234567890")
    private String phone;

    @NotNull(message = "Terms acceptance is required")
    @AssertTrue(message = "You must accept the terms and conditions")
    @Schema(description = "Terms and conditions acceptance", example = "true", required = true)
    private Boolean acceptTerms;

    @NotNull(message = "Privacy policy acceptance is required")
    @AssertTrue(message = "You must accept the privacy policy")
    @Schema(description = "Privacy policy acceptance", example = "true", required = true)
    private Boolean acceptPrivacy;

    @Schema(description = "Marketing emails opt-in", example = "true")
    @Builder.Default
    private Boolean marketingEmails = true;

    @Schema(description = "Notification emails opt-in", example = "true")
    @Builder.Default
    private Boolean notificationEmails = true;

    @Size(max = 50, message = "Timezone must not exceed 50 characters")
    @Schema(description = "User timezone", example = "America/New_York")
    private String timezone;

    @Pattern(regexp = "^[a-z]{2}$", message = "Language must be a valid 2-letter language code")
    @Schema(description = "User language (ISO 639-1)", example = "en")
    private String language;

    /**
     * Sanitize input fields.
     */
    public void sanitize() {
        if (email != null) {
            this.email = email.trim().toLowerCase();
        }
        if (username != null) {
            this.username = username.trim().toLowerCase();
        }
        if (firstName != null) {
            this.firstName = firstName.trim();
        }
        if (lastName != null) {
            this.lastName = lastName.trim();
        }
        if (phone != null) {
            this.phone = phone.trim();
        }
        if (timezone != null) {
            this.timezone = timezone.trim();
        }
        if (language != null) {
            this.language = language.trim().toLowerCase();
        }
    }

    /**
     * Check if passwords match.
     */
    public boolean passwordsMatch() {
        return password != null && password.equals(confirmPassword);
    }

    /**
     * Check if required terms are accepted.
     */
    public boolean hasAcceptedRequiredTerms() {
        return Boolean.TRUE.equals(acceptTerms) && Boolean.TRUE.equals(acceptPrivacy);
    }

    /**
     * Get sanitized username or generate from email.
     */
    public String getEffectiveUsername() {
        if (username != null && !username.trim().isEmpty()) {
            return username.trim().toLowerCase();
        }
        
        // Generate username from email if not provided
        if (email != null) {
            String emailUsername = email.split("@")[0];
            return emailUsername.replaceAll("[^a-zA-Z0-9_]", "_").toLowerCase();
        }
        
        return null;
    }

    /**
     * Get effective timezone.
     */
    public String getEffectiveTimezone() {
        return timezone != null && !timezone.trim().isEmpty() ? timezone : "UTC";
    }

    /**
     * Get effective language.
     */
    public String getEffectiveLanguage() {
        return language != null && !language.trim().isEmpty() ? language : "en";
    }
}