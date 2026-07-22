package com.interviai.backend.module.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for refresh token operations.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Refresh token request payload")
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    @Schema(description = "Refresh token", required = true, 
            example = "550e8400-e29b-41d4-a716-446655440000")
    private String refreshToken;

    @Schema(description = "Device identifier", example = "mobile-app-v1.0")
    private String deviceId;

    @Schema(description = "User agent string", example = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
    private String userAgent;

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