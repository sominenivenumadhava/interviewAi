package com.interviai.backend.module.user.repository;

import com.interviai.backend.module.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
 * Repository for User entity operations.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find user by email address.
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by username.
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email or username.
     */
    @Query("SELECT u FROM User u WHERE u.email = :identifier OR u.username = :identifier")
    Optional<User> findByEmailOrUsername(@Param("identifier") String identifier);

    /**
     * Find user by email verification token.
     */
    Optional<User> findByEmailVerificationToken(String token);

    /**
     * Find user by password reset token.
     */
    Optional<User> findByPasswordResetToken(String token);

    /**
     * Check if email exists.
     */
    boolean existsByEmail(String email);

    /**
     * Check if username exists.
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists for different user.
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.id != :userId")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("userId") UUID userId);

    /**
     * Check if username exists for different user.
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username AND u.id != :userId")
    boolean existsByUsernameAndIdNot(@Param("username") String username, @Param("userId") UUID userId);

    /**
     * Find active users (not deleted and active).
     */
    @Query("SELECT u FROM User u WHERE u.isDeleted = false AND u.isActive = true")
    Page<User> findActiveUsers(Pageable pageable);

    /**
     * Find users by role.
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isDeleted = false")
    List<User> findByRole(@Param("role") User.UserRole role);

    /**
     * Find verified users.
     */
    @Query("SELECT u FROM User u WHERE u.emailVerified = true AND u.isDeleted = false")
    Page<User> findVerifiedUsers(Pageable pageable);

    /**
     * Find unverified users.
     */
    @Query("SELECT u FROM User u WHERE u.emailVerified = false AND u.isDeleted = false")
    Page<User> findUnverifiedUsers(Pageable pageable);

    /**
     * Search users by name or email.
     */
    @Query("SELECT u FROM User u WHERE u.isDeleted = false AND " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);

    /**
     * Find users created within date range.
     */
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate " +
           "AND u.isDeleted = false ORDER BY u.createdAt DESC")
    List<User> findUsersCreatedBetween(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    /**
     * Find users with expired email verification tokens.
     */
    @Query("SELECT u FROM User u WHERE u.emailVerificationToken IS NOT NULL " +
           "AND u.emailVerificationExpiresAt < :now AND u.emailVerified = false")
    List<User> findUsersWithExpiredEmailVerification(@Param("now") LocalDateTime now);

    /**
     * Find users with expired password reset tokens.
     */
    @Query("SELECT u FROM User u WHERE u.passwordResetToken IS NOT NULL " +
           "AND u.passwordResetExpiresAt < :now")
    List<User> findUsersWithExpiredPasswordReset(@Param("now") LocalDateTime now);

    /**
     * Find locked users.
     */
    @Query("SELECT u FROM User u WHERE u.accountLockedUntil IS NOT NULL " +
           "AND u.accountLockedUntil > :now")
    List<User> findLockedUsers(@Param("now") LocalDateTime now);

    /**
     * Find users who haven't logged in for specified days.
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt IS NULL OR u.lastLoginAt < :cutoffDate")
    List<User> findInactiveUsers(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Update last login timestamp.
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") UUID userId, @Param("loginTime") LocalDateTime loginTime);

    /**
     * Increment login attempts.
     */
    @Modifying
    @Query("UPDATE User u SET u.loginAttempts = u.loginAttempts + 1 WHERE u.id = :userId")
    void incrementLoginAttempts(@Param("userId") UUID userId);

    /**
     * Reset login attempts.
     */
    @Modifying
    @Query("UPDATE User u SET u.loginAttempts = 0, u.accountLockedUntil = NULL WHERE u.id = :userId")
    void resetLoginAttempts(@Param("userId") UUID userId);

    /**
     * Lock user account.
     */
    @Modifying
    @Query("UPDATE User u SET u.accountLockedUntil = :lockUntil WHERE u.id = :userId")
    void lockUser(@Param("userId") UUID userId, @Param("lockUntil") LocalDateTime lockUntil);

    /**
     * Unlock user account.
     */
    @Modifying
    @Query("UPDATE User u SET u.accountLockedUntil = NULL, u.loginAttempts = 0 WHERE u.id = :userId")
    void unlockUser(@Param("userId") UUID userId);

    /**
     * Update email verification status.
     */
    @Modifying
    @Query("UPDATE User u SET u.emailVerified = :verified, " +
           "u.emailVerificationToken = NULL, u.emailVerificationExpiresAt = NULL " +
           "WHERE u.id = :userId")
    void updateEmailVerificationStatus(@Param("userId") UUID userId, @Param("verified") boolean verified);

    /**
     * Clear password reset token.
     */
    @Modifying
    @Query("UPDATE User u SET u.passwordResetToken = NULL, u.passwordResetExpiresAt = NULL " +
           "WHERE u.id = :userId")
    void clearPasswordResetToken(@Param("userId") UUID userId);

    /**
     * Clean expired email verification tokens.
     */
    @Modifying
    @Query("UPDATE User u SET u.emailVerificationToken = NULL, u.emailVerificationExpiresAt = NULL " +
           "WHERE u.emailVerificationToken IS NOT NULL AND u.emailVerificationExpiresAt < :now")
    int cleanExpiredEmailVerificationTokens(@Param("now") LocalDateTime now);

    /**
     * Clean expired password reset tokens.
     */
    @Modifying
    @Query("UPDATE User u SET u.passwordResetToken = NULL, u.passwordResetExpiresAt = NULL " +
           "WHERE u.passwordResetToken IS NOT NULL AND u.passwordResetExpiresAt < :now")
    int cleanExpiredPasswordResetTokens(@Param("now") LocalDateTime now);

    /**
     * Count users by role.
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.isDeleted = false")
    long countByRole(@Param("role") User.UserRole role);

    /**
     * Count verified users.
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.emailVerified = true AND u.isDeleted = false")
    long countVerifiedUsers();

    /**
     * Count active users.
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true AND u.isDeleted = false")
    long countActiveUsers();

    /**
     * Count users registered today.
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :todayStart AND u.createdAt < :tomorrowStart")
    long countUsersRegisteredToday(@Param("todayStart") LocalDateTime todayStart,
                                   @Param("tomorrowStart") LocalDateTime tomorrowStart);

    /**
     * Find users for admin management (include inactive but not deleted).
     */
    @Query("SELECT u FROM User u WHERE u.isDeleted = false ORDER BY u.createdAt DESC")
    Page<User> findAllForAdmin(Pageable pageable);

    /**
     * Update user profile picture.
     */
    @Modifying
    @Query("UPDATE User u SET u.profilePictureUrl = :pictureUrl WHERE u.id = :userId")
    void updateProfilePicture(@Param("userId") UUID userId, @Param("pictureUrl") String pictureUrl);

    /**
     * Soft delete user.
     */
    @Modifying
    @Query("UPDATE User u SET u.isDeleted = true, u.isActive = false WHERE u.id = :userId")
    void softDeleteUser(@Param("userId") UUID userId);
}