package com.interviai.backend.module.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for logout operations.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Logout response")
public class LogoutResponse {

    @Schema(description = "Logout success status", example = "true")
    @Builder.Default
    private Boolean success = true;

    @Schema(description = "Logout message", example = "Logout successful")
    @Builder.Default
    private String message = "Logout successful";

    @Schema(description = "Number of tokens revoked", example = "1")
    private Integer tokensRevoked;

    @Schema(description = "Whether logout was from all devices", example = "false")
    @Builder.Default
    private Boolean loggedOutFromAllDevices = false;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Schema(description = "Logout timestamp")
    @Builder.Default
    private LocalDateTime logoutAt = LocalDateTime.now();

    /**
     * Create successful logout response.
     */
    public static LogoutResponse success() {
        return LogoutResponse.builder()
                .success(true)
                .message("Logout successful")
                .tokensRevoked(1)
                .loggedOutFromAllDevices(false)
                .build();
    }

    /**
     * Create successful logout response with token count.
     */
    public static LogoutResponse success(int tokensRevoked, boolean allDevices) {
        String message = allDevices 
            ? "Logout successful from all devices" 
            : "Logout successful";
            
        return LogoutResponse.builder()
                .success(true)
                .message(message)
                .tokensRevoked(tokensRevoked)
                .loggedOutFromAllDevices(allDevices)
                .build();
    }

    /**
     * Create logout response for already logged out user.
     */
    public static LogoutResponse alreadyLoggedOut() {
        return LogoutResponse.builder()
                .success(true)
                .message("User was already logged out")
                .tokensRevoked(0)
                .loggedOutFromAllDevices(false)
                .build();
    }
}