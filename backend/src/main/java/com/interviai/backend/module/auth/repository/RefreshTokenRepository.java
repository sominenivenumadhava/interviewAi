package com.interviai.backend.module.auth.repository;

import com.interviai.backend.module.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for RefreshToken entity operations.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Find refresh token by token string.
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Find all refresh tokens for a user.
     */
    List<RefreshToken> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Find all valid (non-expired, non-revoked) refresh tokens for a user.
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.userId = :userId " +
           "AND rt.isRevoked = false AND rt.expiresAt > :now")
    List<RefreshToken> findValidTokensByUserId(@Param("userId") UUID userId, 
                                              @Param("now") LocalDateTime now);

    /**
     * Find valid refresh token by token string.
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.token = :token " +
           "AND rt.isRevoked = false AND rt.expiresAt > :now")
    Optional<RefreshToken> findValidByToken(@Param("token") String token, 
                                          @Param("now") LocalDateTime now);

    /**
     * Check if a valid refresh token exists for user and device.
     */
    @Query("SELECT COUNT(rt) > 0 FROM RefreshToken rt WHERE rt.userId = :userId " +
           "AND rt.deviceId = :deviceId AND rt.isRevoked = false AND rt.expiresAt > :now")
    boolean existsValidTokenForUserAndDevice(@Param("userId") UUID userId,
                                           @Param("deviceId") String deviceId,
                                           @Param("now") LocalDateTime now);

    /**
     * Revoke all refresh tokens for a user.
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.userId = :userId")
    void revokeAllTokensForUser(@Param("userId") UUID userId);

    /**
     * Revoke all refresh tokens for a user except the specified token.
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.userId = :userId " +
           "AND rt.token != :exceptToken")
    void revokeAllTokensForUserExcept(@Param("userId") UUID userId, 
                                     @Param("exceptToken") String exceptToken);

    /**
     * Revoke refresh token by token string.
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true WHERE rt.token = :token")
    void revokeByToken(@Param("token") String token);

    /**
     * Delete expired refresh tokens.
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Delete revoked refresh tokens older than specified date.
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.isRevoked = true AND rt.createdAt < :before")
    void deleteRevokedTokensOlderThan(@Param("before") LocalDateTime before);

    /**
     * Count active tokens for a user.
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.userId = :userId " +
           "AND rt.isRevoked = false AND rt.expiresAt > :now")
    long countActiveTokensForUser(@Param("userId") UUID userId, 
                                 @Param("now") LocalDateTime now);

    /**
     * Find tokens that need cleanup (expired or old revoked tokens).
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiresAt < :now " +
           "OR (rt.isRevoked = true AND rt.createdAt < :oldDate)")
    List<RefreshToken> findTokensForCleanup(@Param("now") LocalDateTime now,
                                          @Param("oldDate") LocalDateTime oldDate);

    /**
     * Update last used timestamp for a token.
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.lastUsedAt = :lastUsedAt WHERE rt.token = :token")
    void updateLastUsed(@Param("token") String token, @Param("lastUsedAt") LocalDateTime lastUsedAt);
}