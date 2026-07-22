package com.interviai.backend.module.auth.service;

import com.interviai.backend.common.exception.ResourceNotFoundException;
import com.interviai.backend.common.exception.UnauthorizedException;
import com.interviai.backend.module.auth.entity.RefreshToken;
import com.interviai.backend.module.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for refresh token operations.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refresh-token-validity}")
    private long refreshTokenValiditySeconds;

    /**
     * Create and save a new refresh token.
     */
    @Transactional
    public RefreshToken createRefreshToken(UUID userId, String userAgent, String ipAddress, String deviceId) {
        log.debug("Creating refresh token for user: {}", userId);
        
        String token = generateTokenString();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(refreshTokenValiditySeconds);
        
        RefreshToken refreshToken = RefreshToken.create(
            token, userId, expiresAt, userAgent, ipAddress, deviceId);
        
        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        log.debug("Refresh token created successfully for user: {}", userId);
        
        return saved;
    }

    /**
     * Find and validate refresh token.
     */
    @Transactional(readOnly = true)
    public RefreshToken findValidRefreshToken(String token) {
        log.debug("Validating refresh token");
        
        RefreshToken refreshToken = refreshTokenRepository.findValidByToken(token, LocalDateTime.now())
                .orElseThrow(() -> {
                    log.warn("Invalid or expired refresh token provided");
                    return UnauthorizedException.invalidToken();
                });
        
        log.debug("Valid refresh token found for user: {}", refreshToken.getUserId());
        return refreshToken;
    }

    /**
     * Update last used timestamp for refresh token.
     */
    @Transactional
    public void updateLastUsed(String token) {
        log.debug("Updating last used timestamp for refresh token");
        refreshTokenRepository.updateLastUsed(token, LocalDateTime.now());
    }

    /**
     * Revoke refresh token.
     */
    @Transactional
    public void revokeRefreshToken(String token) {
        log.debug("Revoking refresh token");
        
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(token);
        if (tokenOpt.isPresent()) {
            RefreshToken refreshToken = tokenOpt.get();
            refreshToken.revoke();
            refreshTokenRepository.save(refreshToken);
            log.debug("Refresh token revoked for user: {}", refreshToken.getUserId());
        } else {
            log.debug("Refresh token not found for revocation");
        }
    }

    /**
     * Revoke all refresh tokens for a user.
     */
    @Transactional
    public int revokeAllUserTokens(UUID userId) {
        log.debug("Revoking all refresh tokens for user: {}", userId);
        
        List<RefreshToken> activeTokens = refreshTokenRepository.findValidTokensByUserId(userId, LocalDateTime.now());
        int count = activeTokens.size();
        
        if (count > 0) {
            refreshTokenRepository.revokeAllTokensForUser(userId);
            log.debug("Revoked {} refresh tokens for user: {}", count, userId);
        }
        
        return count;
    }

    /**
     * Revoke all refresh tokens for a user except the current one.
     */
    @Transactional
    public int revokeAllUserTokensExcept(UUID userId, String currentToken) {
        log.debug("Revoking all refresh tokens for user {} except current", userId);
        
        List<RefreshToken> activeTokens = refreshTokenRepository.findValidTokensByUserId(userId, LocalDateTime.now());
        int totalCount = activeTokens.size();
        
        if (totalCount > 0) {
            refreshTokenRepository.revokeAllTokensForUserExcept(userId, currentToken);
            // Subtract 1 for the current token that wasn't revoked
            int revokedCount = Math.max(0, totalCount - 1);
            log.debug("Revoked {} refresh tokens for user: {}", revokedCount, userId);
            return revokedCount;
        }
        
        return 0;
    }

    /**
     * Check if user has active refresh tokens.
     */
    @Transactional(readOnly = true)
    public boolean hasActiveTokens(UUID userId) {
        long count = refreshTokenRepository.countActiveTokensForUser(userId, LocalDateTime.now());
        return count > 0;
    }

    /**
     * Get count of active refresh tokens for user.
     */
    @Transactional(readOnly = true)
    public long getActiveTokenCount(UUID userId) {
        return refreshTokenRepository.countActiveTokensForUser(userId, LocalDateTime.now());
    }

    /**
     * Check if user has valid token for specific device.
     */
    @Transactional(readOnly = true)
    public boolean hasValidTokenForDevice(UUID userId, String deviceId) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            return false;
        }
        
        return refreshTokenRepository.existsValidTokenForUserAndDevice(userId, deviceId, LocalDateTime.now());
    }

    /**
     * Get all active refresh tokens for user (for admin purposes).
     */
    @Transactional(readOnly = true)
    public List<RefreshToken> getActiveTokensForUser(UUID userId) {
        return refreshTokenRepository.findValidTokensByUserId(userId, LocalDateTime.now());
    }

    /**
     * Rotate refresh token (revoke old and create new).
     */
    @Transactional
    public RefreshToken rotateRefreshToken(String oldToken, String userAgent, String ipAddress, String deviceId) {
        log.debug("Rotating refresh token");
        
        RefreshToken oldRefreshToken = findValidRefreshToken(oldToken);
        UUID userId = oldRefreshToken.getUserId();
        
        // Revoke the old token
        oldRefreshToken.revoke();
        refreshTokenRepository.save(oldRefreshToken);
        
        // Create new token
        RefreshToken newToken = createRefreshToken(userId, userAgent, ipAddress, deviceId);
        
        log.debug("Refresh token rotated successfully for user: {}", userId);
        return newToken;
    }

    /**
     * Clean up expired and old revoked tokens.
     */
    @Async
    @Transactional
    public void cleanupTokens() {
        log.debug("Starting refresh token cleanup");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oldRevokedThreshold = now.minusDays(30); // Keep revoked tokens for 30 days
        
        // Delete expired tokens
        refreshTokenRepository.deleteExpiredTokens(now);
        
        // Delete old revoked tokens
        refreshTokenRepository.deleteRevokedTokensOlderThan(oldRevokedThreshold);
        
        log.debug("Refresh token cleanup completed");
    }

    /**
     * Validate token and get associated user ID.
     */
    @Transactional(readOnly = true)
    public UUID validateAndGetUserId(String token) {
        RefreshToken refreshToken = findValidRefreshToken(token);
        return refreshToken.getUserId();
    }

    /**
     * Check if refresh token exists and is valid.
     */
    @Transactional(readOnly = true)
    public boolean isValidToken(String token) {
        try {
            return refreshTokenRepository.findValidByToken(token, LocalDateTime.now()).isPresent();
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Generate a unique token string.
     */
    private String generateTokenString() {
        return UUID.randomUUID().toString();
    }

    /**
     * Get refresh token validity in seconds.
     */
    public long getRefreshTokenValidity() {
        return refreshTokenValiditySeconds;
    }
}