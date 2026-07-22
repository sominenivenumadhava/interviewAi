package com.interviai.backend.module.user.entity;

import com.interviai.backend.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * User entity representing system users.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email", unique = true),
    @Index(name = "idx_users_username", columnList = "username", unique = true),
    @Index(name = "idx_users_created_at", columnList = "created_at"),
    @Index(name = "idx_users_last_login", columnList = "last_login_at"),
    @Index(name = "idx_users_email_verified", columnList = "email_verified")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class User extends AuditableEntity implements UserDetails {

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "username", unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", length = 20)
    @Builder.Default
    private AuthProvider authProvider = AuthProvider.LOCAL;

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email_verified", nullable = false)
    @Builder.Default
    private Boolean emailVerified = false;

    @Column(name = "email_verification_token", length = 255)
    private String emailVerificationToken;

    @Column(name = "email_verification_expires_at")
    private LocalDateTime emailVerificationExpiresAt;

    @Column(name = "password_reset_token", length = 255)
    private String passwordResetToken;

    @Column(name = "password_reset_expires_at")
    private LocalDateTime passwordResetExpiresAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "login_attempts", nullable = false)
    @Builder.Default
    private Integer loginAttempts = 0;

    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;

    @Column(name = "profile_picture_url", length = 500)
    private String profilePictureUrl;

    @Column(name = "bio", length = 1000)
    private String bio;

    @Column(name = "timezone", length = 50)
    @Builder.Default
    private String timezone = "UTC";

    @Column(name = "language", length = 10)
    @Builder.Default
    private String language = "en";

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Column(name = "terms_accepted_at")
    private LocalDateTime termsAcceptedAt;

    @Column(name = "privacy_accepted_at")
    private LocalDateTime privacyAcceptedAt;

    @Column(name = "marketing_emails_enabled", nullable = false)
    @Builder.Default
    private Boolean marketingEmailsEnabled = true;

    @Column(name = "notification_emails_enabled", nullable = false)
    @Builder.Default
    private Boolean notificationEmailsEnabled = true;

    // UserDetails interface implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email; // Using email as username
    }

    @Override
    public boolean isAccountNonExpired() {
        return getIsActive();
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountLockedUntil == null || LocalDateTime.now().isAfter(accountLockedUntil);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // We handle password expiry separately if needed
    }

    @Override
    public boolean isEnabled() {
        return getIsActive() && emailVerified;
    }

    // Business methods
    
    /**
     * Get full name of the user.
     */
    public String getFullName() {
        if (firstName == null && lastName == null) {
            return email;
        }
        if (firstName == null) {
            return lastName;
        }
        if (lastName == null) {
            return firstName;
        }
        return firstName + " " + lastName;
    }

    /**
     * Get display name (first name or email).
     */
    public String getDisplayName() {
        return firstName != null ? firstName : email;
    }

    /**
     * Check if account is currently locked.
     */
    public boolean isAccountLocked() {
        return accountLockedUntil != null && LocalDateTime.now().isBefore(accountLockedUntil);
    }

    /**
     * Lock account for specified duration.
     */
    public void lockAccount(int minutes) {
        this.accountLockedUntil = LocalDateTime.now().plusMinutes(minutes);
    }

    /**
     * Unlock account.
     */
    public void unlockAccount() {
        this.accountLockedUntil = null;
        this.loginAttempts = 0;
    }

    /**
     * Increment login attempts.
     */
    public void incrementLoginAttempts() {
        this.loginAttempts = (this.loginAttempts == null ? 0 : this.loginAttempts) + 1;
    }

    /**
     * Reset login attempts on successful login.
     */
    public void resetLoginAttempts() {
        this.loginAttempts = 0;
        this.accountLockedUntil = null;
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * Check if email verification token is valid.
     */
    public boolean isEmailVerificationTokenValid(String token) {
        return emailVerificationToken != null &&
               emailVerificationToken.equals(token) &&
               emailVerificationExpiresAt != null &&
               LocalDateTime.now().isBefore(emailVerificationExpiresAt);
    }

    /**
     * Check if password reset token is valid.
     */
    public boolean isPasswordResetTokenValid(String token) {
        return passwordResetToken != null &&
               passwordResetToken.equals(token) &&
               passwordResetExpiresAt != null &&
               LocalDateTime.now().isBefore(passwordResetExpiresAt);
    }

    /**
     * Generate email verification token.
     */
    public void generateEmailVerificationToken(String token, int validityHours) {
        this.emailVerificationToken = token;
        this.emailVerificationExpiresAt = LocalDateTime.now().plusHours(validityHours);
    }

    /**
     * Generate password reset token.
     */
    public void generatePasswordResetToken(String token, int validityHours) {
        this.passwordResetToken = token;
        this.passwordResetExpiresAt = LocalDateTime.now().plusHours(validityHours);
    }

    /**
     * Verify email address.
     */
    public void verifyEmail() {
        this.emailVerified = true;
        this.emailVerificationToken = null;
        this.emailVerificationExpiresAt = null;
    }

    /**
     * Clear password reset token.
     */
    public void clearPasswordResetToken() {
        this.passwordResetToken = null;
        this.passwordResetExpiresAt = null;
    }

    /**
     * Accept terms and conditions.
     */
    public void acceptTerms() {
        this.termsAcceptedAt = LocalDateTime.now();
    }

    /**
     * Accept privacy policy.
     */
    public void acceptPrivacy() {
        this.privacyAcceptedAt = LocalDateTime.now();
    }

    /**
     * Check if user has accepted current terms.
     */
    public boolean hasAcceptedTerms() {
        return termsAcceptedAt != null;
    }

    /**
     * Check if user has accepted privacy policy.
     */
    public boolean hasAcceptedPrivacy() {
        return privacyAcceptedAt != null;
    }

    /**
     * Calculate profile completeness percentage.
     */
    public int getProfileCompleteness() {
        int completeness = 0;
        
        // Basic required fields (60%)
        if (email != null && !email.trim().isEmpty()) completeness += 15;
        if (firstName != null && !firstName.trim().isEmpty()) completeness += 15;
        if (lastName != null && !lastName.trim().isEmpty()) completeness += 15;
        if (emailVerified) completeness += 15;
        
        // Optional fields (40%)
        if (phone != null && !phone.trim().isEmpty()) completeness += 10;
        if (bio != null && !bio.trim().isEmpty()) completeness += 10;
        if (profilePictureUrl != null && !profilePictureUrl.trim().isEmpty()) completeness += 10;
        if (hasAcceptedTerms() && hasAcceptedPrivacy()) completeness += 10;
        
        return Math.min(completeness, 100);
    }

    /**
     * Check if user is admin.
     */
    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    /**
     * Check if user is moderator.
     */
    public boolean isModerator() {
        return role == UserRole.MODERATOR;
    }

    /**
     * Check if user has role or higher.
     */
    public boolean hasRoleOrHigher(UserRole requiredRole) {
        return role.getLevel() >= requiredRole.getLevel();
    }

    /**
     * User roles enum.
     */
    public enum UserRole {
        USER(1),
        MODERATOR(2),
        ADMIN(3);

        private final int level;

        UserRole(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }
}