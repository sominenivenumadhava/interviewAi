package com.interviai.backend.module.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for authentication operations.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Authentication response")
public class AuthenticationResponse {

    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIs...")
    private String accessToken;

    @Schema(description = "Refresh token", example = "550e8400-e29b-41d4-a716-446655440000")
    private String refreshToken;

    @Schema(description = "Token type", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Access token expiration time in seconds", example = "3600")
    private Long expiresIn;

    @Schema(description = "Token scope", example = "read write")
    private String scope;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Schema(description = "Token issued at timestamp")
    private LocalDateTime issuedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Schema(description = "Access token expiration timestamp")
    private LocalDateTime accessTokenExpiresAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Schema(description = "Refresh token expiration timestamp")
    private LocalDateTime refreshTokenExpiresAt;

    @Schema(description = "User information")
    private UserInfo user;

    @Schema(description = "User permissions/roles")
    private List<String> permissions;

    @Schema(description = "Session information")
    private SessionInfo session;

    /**
     * Nested class for user information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "User information")
    public static class UserInfo {

        @Schema(description = "User ID", example = "123e4567-e89b-12d3-a456-426614174000")
        private UUID id;

        @Schema(description = "User email", example = "user@example.com")
        private String email;

        @Schema(description = "First name", example = "John")
        private String firstName;

        @Schema(description = "Last name", example = "Doe")
        private String lastName;

        @Schema(description = "Email verification status", example = "true")
        private Boolean emailVerified;

        @Schema(description = "Account active status", example = "true")
        private Boolean isActive;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        @Schema(description = "Last login timestamp")
        private LocalDateTime lastLoginAt;

        @Schema(description = "Profile completion percentage", example = "85")
        private Integer profileCompleteness;

        @Schema(description = "User role", example = "USER", allowableValues = {"USER", "ADMIN", "MODERATOR"})
        private String role;
    }

    /**
     * Nested class for session information.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Session information")
    public static class SessionInfo {

        @Schema(description = "Session ID", example = "550e8400-e29b-41d4-a716-446655440000")
        private String sessionId;

        @Schema(description = "Device ID", example = "mobile-app-v1.0")
        private String deviceId;

        @Schema(description = "IP address", example = "192.168.1.1")
        private String ipAddress;

        @Schema(description = "User agent", example = "Mozilla/5.0...")
        private String userAgent;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        @Schema(description = "Session created at")
        private LocalDateTime createdAt;

        @Schema(description = "Remember me status", example = "false")
        private Boolean rememberMe;
    }

    /**
     * Create successful login response.
     */
    public static AuthenticationResponse success(String accessToken, String refreshToken,
                                               Long expiresIn, UserInfo user) {
        LocalDateTime now = LocalDateTime.now();
        
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .issuedAt(now)
                .accessTokenExpiresAt(now.plusSeconds(expiresIn))
                .user(user)
                .build();
    }

    /**
     * Create token refresh response.
     */
    public static AuthenticationResponse refreshSuccess(String accessToken, Long expiresIn) {
        LocalDateTime now = LocalDateTime.now();
        
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .issuedAt(now)
                .accessTokenExpiresAt(now.plusSeconds(expiresIn))
                .build();
    }

    /**
     * Create token refresh response with new refresh token.
     */
    public static AuthenticationResponse refreshSuccessWithNewToken(String accessToken, 
                                                                   String refreshToken, 
                                                                   Long expiresIn,
                                                                   Long refreshExpiresIn) {
        LocalDateTime now = LocalDateTime.now();
        
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .issuedAt(now)
                .accessTokenExpiresAt(now.plusSeconds(expiresIn))
                .refreshTokenExpiresAt(now.plusSeconds(refreshExpiresIn))
                .build();
    }
}