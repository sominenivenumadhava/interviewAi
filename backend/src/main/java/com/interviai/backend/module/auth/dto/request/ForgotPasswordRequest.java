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
 * Request DTO for forgot password operation.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Forgot password request payload")
public class ForgotPasswordRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Schema(description = "User email address", example = "user@example.com", required = true)
    private String email;

    @Schema(description = "Client base URL for password reset link", 
            example = "https://app.interviai.com")
    private String clientBaseUrl;

    /**
     * Sanitize the email by trimming and converting to lowercase.
     */
    public void sanitizeEmail() {
        if (email != null) {
            this.email = email.trim().toLowerCase();
        }
    }

    /**
     * Get sanitized client base URL.
     */
    public String getSanitizedClientBaseUrl() {
        if (clientBaseUrl == null) {
            return null;
        }
        String sanitized = clientBaseUrl.trim();
        // Remove trailing slash
        if (sanitized.endsWith("/")) {
            sanitized = sanitized.substring(0, sanitized.length() - 1);
        }
        return sanitized;
    }
}