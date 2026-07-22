package com.interviai.backend.module.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for email verification.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Email verification request payload")
public class VerifyEmailRequest {

    @NotBlank(message = "Verification token is required")
    @Schema(description = "Email verification token", required = true,
            example = "550e8400-e29b-41d4-a716-446655440000")
    private String token;

    /**
     * Get sanitized token.
     */
    public String getSanitizedToken() {
        return token != null ? token.trim() : null;
    }
}