package com.interviai.backend.module.auth.entity;

import com.interviai.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing refresh tokens for JWT authentication.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_refresh_tokens_token", columnList = "token"),
    @Index(name = "idx_refresh_tokens_user_id", columnList = "user_id"),
    @Index(name = "idx_refresh_tokens_expires_at", columnList = "expires_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RefreshToken extends BaseEntity {

    @Column(name = "token", nullable = false, unique = true, length = 500)
    private String token;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_revoked", nullable = false)
    @Builder.Default
    private Boolean isRevoked = false;

    @Column(name = "user_agent", length = 1000)
    private String userAgent;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "device_id", length = 255)
    private String deviceId;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    /**
     * Check if the refresh token is expired.
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Check if the refresh token is valid (not expired and not revoked).
     */
    public boolean isValid() {
        return !isExpired() && !isRevoked;
    }

    /**
     * Revoke the refresh token.
     */
    public void revoke() {
        this.isRevoked = true;
    }

    /**
     * Update the last used timestamp.
     */
    public void updateLastUsed() {
        this.lastUsedAt = LocalDateTime.now();
    }

    /**
     * Create a new refresh token.
     */
    public static RefreshToken create(String token, UUID userId, LocalDateTime expiresAt) {
        return RefreshToken.builder()
                .token(token)
                .userId(userId)
                .expiresAt(expiresAt)
                .isRevoked(false)
                .build();
    }

    /**
     * Create a new refresh token with device information.
     */
    public static RefreshToken create(String token, UUID userId, LocalDateTime expiresAt,
                                    String userAgent, String ipAddress, String deviceId) {
        return RefreshToken.builder()
                .token(token)
                .userId(userId)
                .expiresAt(expiresAt)
                .isRevoked(false)
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .deviceId(deviceId)
                .build();
    }
}