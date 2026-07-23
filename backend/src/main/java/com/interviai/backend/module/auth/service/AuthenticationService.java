package com.interviai.backend.module.auth.service;

import com.interviai.backend.common.exception.UnauthorizedException;
import com.interviai.backend.common.exception.ValidationException;
import com.interviai.backend.common.util.StringUtil;
import com.interviai.backend.common.util.ValidationUtil;
import com.interviai.backend.module.auth.dto.request.*;
import com.interviai.backend.module.auth.dto.response.*;
import com.interviai.backend.module.auth.entity.RefreshToken;
import com.interviai.backend.module.auth.mapper.AuthMapper;
import com.interviai.backend.module.user.entity.User;
import com.interviai.backend.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for authentication operations.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;
    private final UserService userService;

    /**
     * Authenticate user and generate tokens.
     */
    @Transactional
    public AuthenticationResponse authenticate(LoginRequest request, String ipAddress) {
        log.info("Authentication attempt for email: {}", StringUtil.maskEmail(request.getEmail()));
        
        // Sanitize input
        request.sanitizeEmail();
        
        // Validate input
        ValidationUtil.validateEmail(request.getEmail(), "email");
        ValidationUtil.requireNotBlank(request.getPassword(), "password");
        
        // Check if user exists and registered via OAuth without setting a password
        java.util.Optional<User> userOpt = userService.findByEmail(request.getEmail());
        if (userOpt.isPresent()) {
            User existingUser = userOpt.get();
            if (existingUser.getPasswordHash() == null) {
                String providerName = existingUser.getAuthProvider() != null ? 
                                     existingUser.getAuthProvider().name().substring(0, 1).toUpperCase() + 
                                     existingUser.getAuthProvider().name().substring(1).toLowerCase() : "an external provider";
                throw new ValidationException(
                    String.format("This account was created with %s. Please log in using %s.", 
                        providerName, providerName)
                );
            }
        }
        
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()
                )
            );

            // Get authenticated user details
            User user = userService.getUserByEmail(request.getEmail());
            
            // Update last login timestamp
            userService.updateLastLogin(user.getId());
            
            // Generate tokens
            String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                user.getId(),
                request.getTruncatedUserAgent(),
                ipAddress,
                request.getSanitizedDeviceId()
            );

            // Create user info — pass the already-fetched user to avoid a second DB round-trip
            AuthenticationResponse.UserInfo userInfo = createUserInfo(user);
            
            // Create session info
            AuthenticationResponse.SessionInfo sessionInfo = authMapper.toSessionInfo(
                refreshToken.getId().toString(),
                request.getSanitizedDeviceId(),
                ipAddress,
                request.getTruncatedUserAgent(),
                request.getRememberMe()
            );

            // Create response
            AuthenticationResponse response = authMapper.createAuthenticationResponse(
                accessToken,
                refreshToken.getToken(),
                jwtService.getAccessTokenValidity(),
                userInfo,
                sessionInfo,
                List.of(user.getRole().name()) // User permissions
            );

            log.info("Authentication successful for user: {}", user.getId());
            return response;

        } catch (AuthenticationException e) {
            log.warn("Authentication failed for email: {} - {}", 
                    StringUtil.maskEmail(request.getEmail()), e.getMessage());
            throw UnauthorizedException.invalidCredentials();
        }
    }

    /**
     * Refresh access token using refresh token.
     */
    @Transactional
    public AuthenticationResponse refreshToken(RefreshTokenRequest request, String ipAddress) {
        log.debug("Token refresh attempt");
        
        ValidationUtil.requireNotBlank(request.getRefreshToken(), "refreshToken");
        
        try {
            // Validate refresh token
            RefreshToken refreshToken = refreshTokenService.findValidRefreshToken(request.getRefreshToken());
            
            // Update last used timestamp
            refreshTokenService.updateLastUsed(request.getRefreshToken());
            
            // Get user info
            UUID userId = refreshToken.getUserId();
            User user = userService.getUserById(userId);
            
            // Generate new access token
            String accessToken = jwtService.generateAccessToken(userId, user.getEmail(), user.getRole().name());
            
            // Create refresh response
            AuthenticationResponse response = authMapper.createRefreshResponse(
                accessToken,
                request.getRefreshToken(),
                jwtService.getAccessTokenValidity()
            );

            log.debug("Token refresh successful for user: {}", userId);
            return response;

        } catch (Exception e) {
            log.warn("Token refresh failed: {}", e.getMessage());
            throw UnauthorizedException.invalidToken();
        }
    }

    /**
     * Logout user and revoke tokens.
     */
    @Transactional
    public LogoutResponse logout(LogoutRequest request, UUID currentUserId) {
        log.debug("Logout attempt for user: {}", currentUserId);
        
        try {
            int tokensRevoked = 0;
            boolean allDevices = request.shouldLogoutFromAllDevices();
            
            if (allDevices) {
                // Revoke all tokens for user
                tokensRevoked = refreshTokenService.revokeAllUserTokens(currentUserId);
                log.info("Logout from all devices completed for user: {} - {} tokens revoked", 
                        currentUserId, tokensRevoked);
            } else if (request.hasRefreshToken()) {
                // Revoke specific token
                refreshTokenService.revokeRefreshToken(request.getRefreshToken());
                tokensRevoked = 1;
                log.info("Logout completed for user: {}", currentUserId);
            } else {
                // No specific token provided, revoke all tokens as fallback
                tokensRevoked = refreshTokenService.revokeAllUserTokens(currentUserId);
                log.info("Logout completed (all tokens) for user: {} - {} tokens revoked", 
                        currentUserId, tokensRevoked);
            }

            return LogoutResponse.success(tokensRevoked, allDevices);

        } catch (Exception e) {
            log.warn("Logout failed for user: {} - {}", currentUserId, e.getMessage());
            // Return success anyway for security (don't reveal internal errors)
            return LogoutResponse.success();
        }
    }

    /**
     * Initiate forgot password process.
     */
    @Transactional
    public PasswordResetResponse forgotPassword(ForgotPasswordRequest request) {
        log.info("Forgot password request for email: {}", StringUtil.maskEmail(request.getEmail()));
        
        request.sanitizeEmail();
        ValidationUtil.validateEmail(request.getEmail(), "email");
        
        try {
            // Generate password reset token for user
            userService.generatePasswordResetToken(request.getEmail());
            
            // For security, always return success message even if email doesn't exist
            String maskedEmail = StringUtil.maskEmail(request.getEmail());
            
            log.info("Password reset email sent to: {}", maskedEmail);
            
            return PasswordResetResponse.forgotPasswordSuccess(maskedEmail, 60);

        } catch (Exception e) {
            log.error("Forgot password processing failed for email: {} - {}", 
                    StringUtil.maskEmail(request.getEmail()), e.getMessage());
            
            // For security, still return success
            return PasswordResetResponse.emailNotFoundSecure();
        }
    }

    /**
     * Reset password using reset token.
     */
    @Transactional
    public PasswordResetResponse resetPassword(ResetPasswordRequest request) {
        log.info("Password reset attempt with token");
        
        ValidationUtil.requireNotBlank(request.getToken(), "token");
        ValidationUtil.requireNotBlank(request.getNewPassword(), "newPassword");
        
        if (!request.passwordsMatch()) {
            throw new ValidationException("Password confirmation does not match");
        }
        
        try {
            // Reset password using the token
            userService.resetPassword(request.getToken(), request.getNewPassword());
            
            log.info("Password reset completed successfully");
            return PasswordResetResponse.resetPasswordSuccess();

        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Password reset failed: {}", e.getMessage());
            throw new ValidationException("Invalid or expired reset token");
        }
    }

    /**
     * Validate JWT token.
     */
    public boolean validateToken(String token, String username) {
        try {
            return jwtService.isTokenValid(token, username);
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract username from token.
     */
    public String extractUsername(String token) {
        return jwtService.extractUsername(token);
    }

    /**
     * Extract user ID from token.
     */
    public UUID extractUserId(String token) {
        return jwtService.extractUserId(token);
    }

    /**
     * Check if token needs refresh.
     */
    public boolean shouldRefreshToken(String token) {
        return jwtService.shouldRefreshToken(token);
    }

    /**
     * Revoke all user sessions (admin function).
     */
    @Transactional
    public void revokeAllUserSessions(UUID userId) {
        log.info("Revoking all sessions for user: {}", userId);
        
        int tokensRevoked = refreshTokenService.revokeAllUserTokens(userId);
        
        log.info("All sessions revoked for user: {} - {} tokens revoked", userId, tokensRevoked);
    }

    /**
     * Get user's active sessions.
     */
    @Transactional(readOnly = true)
    public List<RefreshToken> getUserActiveSessions(UUID userId) {
        return refreshTokenService.getActiveTokensForUser(userId);
    }

    /**
     * Create user info from user entity.
     */
    private AuthenticationResponse.UserInfo createUserInfo(User user) {
        return authMapper.toUserInfo(
            user.getId(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmailVerified(),
            user.getIsActive(),
            user.getLastLoginAt(),
            user.getProfileCompleteness()
        );
    }
}
