package com.interviai.backend.module.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating user profile.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Profile update request payload")
public class UpdateProfileRequest {

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    @Schema(description = "Username", example = "john_doe")
    private String username;

    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "First name can only contain letters, spaces, apostrophes, and hyphens")
    @Schema(description = "First name", example = "John")
    private String firstName;

    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", message = "Last name can only contain letters, spaces, apostrophes, and hyphens")
    @Schema(description = "Last name", example = "Doe")
    private String lastName;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format (E.164)")
    @Schema(description = "Phone number in E.164 format", example = "+1234567890")
    private String phone;

    @Size(max = 1000, message = "Bio must not exceed 1000 characters")
    @Schema(description = "User biography", example = "Software engineer with 5+ years of experience")
    private String bio;

    @Size(max = 50, message = "Timezone must not exceed 50 characters")
    @Schema(description = "User timezone", example = "America/New_York")
    private String timezone;

    @Pattern(regexp = "^[a-z]{2}$", message = "Language must be a valid 2-letter language code")
    @Schema(description = "User language (ISO 639-1)", example = "en")
    private String language;

    @Schema(description = "Marketing emails preference", example = "true")
    private Boolean marketingEmailsEnabled;

    @Schema(description = "Notification emails preference", example = "true")
    private Boolean notificationEmailsEnabled;

    /**
     * Sanitize input fields.
     */
    public void sanitize() {
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
        if (bio != null) {
            this.bio = bio.trim();
        }
        if (timezone != null) {
            this.timezone = timezone.trim();
        }
        if (language != null) {
            this.language = language.trim().toLowerCase();
        }
    }

    /**
     * Check if any field has a value (not null and not empty).
     */
    public boolean hasAnyUpdate() {
        return hasValue(username) || hasValue(firstName) || hasValue(lastName) ||
               hasValue(phone) || hasValue(bio) || hasValue(timezone) ||
               hasValue(language) || marketingEmailsEnabled != null || 
               notificationEmailsEnabled != null;
    }

    /**
     * Helper method to check if string has value.
     */
    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }
}