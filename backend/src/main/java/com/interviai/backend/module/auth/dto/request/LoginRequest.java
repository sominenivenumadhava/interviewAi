package com.interviai.backend.module.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for user login.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login request payload")
public class LoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Schema(description = "User email address", example = "user@example.com", required = true)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Schema(description = "User password", example = "SecurePass123!", required = true)
    private String password;

    @Schema(description = "Remember me option for extended session", example = "true")
    @Builder.Default
    private Boolean rememberMe = false;

    @Schema(description = "Device identifier for token management", example = "mobile-app-v1.0")
    private String deviceId;

    @Schema(description = "User agent string", example = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
    private String userAgent;

    /**
     * Sanitize the email by trimming and converting to lowercase.
     */
    public void sanitizeEmail() {
        if (email != null) {
            this.email = email.trim().toLowerCase();
        }
    }

    /**
     * Check if device information is provided.
     */
    public boolean hasDeviceInfo() {
        return deviceId != null && !deviceId.trim().isEmpty();
    }

    /**
     * Get sanitized device ID.
     */
    public String getSanitizedDeviceId() {
        return deviceId != null ? deviceId.trim() : null;
    }

    /**
     * Get truncated user agent for storage.
     */
    public String getTruncatedUserAgent() {
        if (userAgent == null) {
            return null;
        }
        return userAgent.length() > 1000 ? userAgent.substring(0, 1000) : userAgent;
    }
}