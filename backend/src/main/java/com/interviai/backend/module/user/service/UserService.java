package com.interviai.backend.module.user.service;

import com.interviai.backend.common.dto.PageResponse;
import com.interviai.backend.module.user.dto.request.ChangePasswordRequest;
import com.interviai.backend.module.user.dto.request.RegisterRequest;
import com.interviai.backend.module.user.dto.request.UpdatePreferencesRequest;
import com.interviai.backend.module.user.dto.request.UpdateProfileRequest;
import com.interviai.backend.module.user.dto.response.UserResponse;
import com.interviai.backend.module.user.entity.User;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for user management operations.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
public interface UserService {

    /**
     * Register a new user.
     */
    UserResponse registerUser(RegisterRequest request);

    /**
     * Find user by ID.
     */
    Optional<User> findById(UUID userId);

    /**
     * Find user by email.
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by username.
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email or username.
     */
    Optional<User> findByEmailOrUsername(String identifier);

    /**
     * Get user by ID (throws exception if not found).
     */
    User getUserById(UUID userId);

    /**
     * Get user by email (throws exception if not found).
     */
    User getUserByEmail(String email);

    /**
     * Update user profile.
     */
    UserResponse updateProfile(UUID userId, UpdateProfileRequest request);

    /**
     * Update user email preferences.
     */
    UserResponse updatePreferences(UUID userId, UpdatePreferencesRequest request);

    /**
     * Change user password.
     */
    void changePassword(UUID userId, ChangePasswordRequest request);

    /**
     * Verify user email.
     */
    UserResponse verifyEmail(String token);

    /**
     * Resend email verification.
     */
    void resendEmailVerification(UUID userId);

    /**
     * Generate password reset token.
     */
    void generatePasswordResetToken(String email);

    /**
     * Reset password using token.
     */
    void resetPassword(String token, String newPassword);

    /**
     * Update user's last login time.
     */
    void updateLastLogin(UUID userId);

    /**
     * Handle failed login attempt.
     */
    void handleFailedLogin(UUID userId);

    /**
     * Lock user account.
     */
    void lockUser(UUID userId, int minutes);

    /**
     * Unlock user account.
     */
    void unlockUser(UUID userId);

    /**
     * Activate user account.
     */
    void activateUser(UUID userId);

    /**
     * Deactivate user account.
     */
    void deactivateUser(UUID userId);

    /**
     * Delete user account (soft delete).
     */
    void deleteUser(UUID userId);

    /**
     * Update profile picture URL.
     */
    void updateProfilePicture(UUID userId, String pictureUrl);

    /**
     * Check if email is available.
     */
    boolean isEmailAvailable(String email);

    /**
     * Check if username is available.
     */
    boolean isUsernameAvailable(String username);

    /**
     * Check if email is available for user (excluding current user).
     */
    boolean isEmailAvailableForUser(String email, UUID userId);

    /**
     * Check if username is available for user (excluding current user).
     */
    boolean isUsernameAvailableForUser(String username, UUID userId);

    /**
     * Search users.
     */
    PageResponse<UserResponse> searchUsers(String search, Pageable pageable);

    /**
     * Get all active users.
     */
    PageResponse<UserResponse> getActiveUsers(Pageable pageable);

    /**
     * Get all users (admin only).
     */
    PageResponse<UserResponse> getAllUsers(Pageable pageable);

    /**
     * Get users by role.
     */
    List<UserResponse> getUsersByRole(User.UserRole role);

    /**
     * Get user statistics.
     */
    UserStatistics getUserStatistics();

    /**
     * Clean up expired tokens.
     */
    void cleanupExpiredTokens();

    /**
     * User statistics data class.
     */
    class UserStatistics {
        private final long totalUsers;
        private final long activeUsers;
        private final long verifiedUsers;
        private final long usersRegisteredToday;
        private final long lockedUsers;

        public UserStatistics(long totalUsers, long activeUsers, long verifiedUsers,
                            long usersRegisteredToday, long lockedUsers) {
            this.totalUsers = totalUsers;
            this.activeUsers = activeUsers;
            this.verifiedUsers = verifiedUsers;
            this.usersRegisteredToday = usersRegisteredToday;
            this.lockedUsers = lockedUsers;
        }

        public long getTotalUsers() { return totalUsers; }
        public long getActiveUsers() { return activeUsers; }
        public long getVerifiedUsers() { return verifiedUsers; }
        public long getUsersRegisteredToday() { return usersRegisteredToday; }
        public long getLockedUsers() { return lockedUsers; }
    }
}