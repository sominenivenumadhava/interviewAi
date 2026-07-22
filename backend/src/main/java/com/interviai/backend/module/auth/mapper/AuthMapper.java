package com.interviai.backend.module.auth.mapper;

import com.interviai.backend.module.auth.dto.response.AuthenticationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Mapper for authentication-related DTOs and entities.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
@Component
public interface AuthMapper {

    /**
     * Create user info from user entity data.
     * Note: This would typically map from a User entity once the User module is implemented.
     */
    @Mapping(target = "profileCompleteness", source = "profileCompleteness")
    AuthenticationResponse.UserInfo toUserInfo(UUID id, String email, String firstName, 
                                             String lastName, Boolean emailVerified, 
                                             Boolean isActive, LocalDateTime lastLoginAt,
                                             Integer profileCompleteness);

    /**
     * Create session info from session data.
     */
    @Mapping(target = "sessionId", source = "sessionId")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    AuthenticationResponse.SessionInfo toSessionInfo(String sessionId, String deviceId, 
                                                   String ipAddress, String userAgent, 
                                                   Boolean rememberMe);

    /**
     * Create authentication response with all components.
     */
    default AuthenticationResponse createAuthenticationResponse(String accessToken, 
                                                              String refreshToken,
                                                              Long expiresIn,
                                                              AuthenticationResponse.UserInfo userInfo,
                                                              AuthenticationResponse.SessionInfo sessionInfo,
                                                              List<String> permissions) {
        LocalDateTime now = LocalDateTime.now();
        
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .issuedAt(now)
                .accessTokenExpiresAt(now.plusSeconds(expiresIn))
                .user(userInfo)
                .session(sessionInfo)
                .permissions(permissions)
                .build();
    }

    /**
     * Create refresh token response.
     */
    default AuthenticationResponse createRefreshResponse(String accessToken, Long expiresIn) {
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
     * Calculate profile completeness percentage.
     */
    @Named("calculateProfileCompleteness")
    default Integer calculateProfileCompleteness(String firstName, String lastName, 
                                               String phone, Boolean emailVerified) {
        int completeness = 0;
        
        // Base fields (email is required for registration)
        completeness += 25; // Email
        
        if (firstName != null && !firstName.trim().isEmpty()) {
            completeness += 25;
        }
        
        if (lastName != null && !lastName.trim().isEmpty()) {
            completeness += 25;
        }
        
        if (phone != null && !phone.trim().isEmpty()) {
            completeness += 15;
        }
        
        if (emailVerified != null && emailVerified) {
            completeness += 10;
        }
        
        return Math.min(completeness, 100);
    }

    /**
     * Mask email for security purposes.
     */
    @Named("maskEmail")
    default String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        
        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];
        
        if (username.length() <= 2) {
            return "*".repeat(username.length()) + "@" + domain;
        }
        
        return username.substring(0, 2) + "*".repeat(Math.max(0, username.length() - 2)) + "@" + domain;
    }
}