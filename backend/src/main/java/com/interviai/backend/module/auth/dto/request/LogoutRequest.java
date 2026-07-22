package com.interviai.backend.module.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for user logout.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Logout request payload")
public class LogoutRequest {

    @Schema(description = "Refresh token to be revoked", 
            example = "550e8400-e29b-41d4-a716-446655440000")
    private String refreshToken;

    @Schema(description = "Logout from all devices", example = "false")
    @Builder.Default
    private Boolean logoutFromAllDevices = false;

    @Schema(description = "Device identifier", example = "mobile-app-v1.0")
    private String deviceId;

    /**
     * Check if logout should be performed from all devices.
     */
    public boolean shouldLogoutFromAllDevices() {
        return logoutFromAllDevices != null && logoutFromAllDevices;
    }

    /**
     * Check if refresh token is provided.
     */
    public boolean hasRefreshToken() {
        return refreshToken != null && !refreshToken.trim().isEmpty();
    }

    /**
     * Get sanitized device ID.
     */
    public String getSanitizedDeviceId() {
        return deviceId != null ? deviceId.trim() : null;
    }
}