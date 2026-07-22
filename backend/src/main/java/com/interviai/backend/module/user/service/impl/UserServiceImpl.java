package com.interviai.backend.module.user.service.impl;

import com.interviai.backend.common.dto.PageResponse;
import com.interviai.backend.common.exception.ResourceNotFoundException;
import com.interviai.backend.common.exception.ValidationException;
import com.interviai.backend.common.util.StringUtil;
import com.interviai.backend.common.util.ValidationUtil;
import com.interviai.backend.module.notification.service.EmailService;
import com.interviai.backend.module.user.dto.request.ChangePasswordRequest;
import com.interviai.backend.module.user.dto.request.RegisterRequest;
import com.interviai.backend.module.user.dto.request.UpdateProfileRequest;
import com.interviai.backend.module.user.dto.response.UserResponse;
import com.interviai.backend.module.user.entity.User;
import com.interviai.backend.module.user.mapper.UserMapper;
import com.interviai.backend.module.user.repository.UserRepository;
import com.interviai.backend.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of UserService.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.security.account.max-login-attempts:5}")
    private int maxLoginAttempts;

    @Value("${app.security.account.lockout-duration-minutes:30}")
    private int lockoutDurationMinutes;
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    @Transactional
    public UserResponse registerUser(RegisterRequest request) {
        log.info("Registering new user with email: {}", StringUtil.maskEmail(request.getEmail()));
        
        // Sanitize and validate input
        request.sanitize();
        validateRegistrationRequest(request);
        
        // Check if email already exists
        Optional<User> existingUserOpt = userRepository.findByEmail(request.getEmail());
        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            if (existingUser.getPasswordHash() == null) {
                // User signed up with OAuth and doesn't have a password yet. Allow them to set one.
                existingUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
                if (existingUser.getFirstName() == null || existingUser.getFirstName().equals("User")) {
                    existingUser.setFirstName(request.getFirstName());
                    existingUser.setLastName(request.getLastName());
                }
                // Don't change auth provider, just save the password so they can log in via email too
                User savedUser = userRepository.save(existingUser);
                log.info("Set password for existing OAuth user: {}", savedUser.getId());
                return userMapper.toResponse(savedUser);
            }
            throw ValidationException.alreadyExists("email", request.getEmail());
        }
        
        // Check if username already exists (if provided)
        String effectiveUsername = request.getEffectiveUsername();
        // Ensure generated username meets minimum length requirement (min=3)
        if (effectiveUsername != null && effectiveUsername.length() < 3) {
            effectiveUsername = effectiveUsername + UUID.randomUUID().toString().replace("-", "").substring(0, 3 - effectiveUsername.length());
        }
        if (effectiveUsername != null && userRepository.existsByUsername(effectiveUsername)) {
            throw ValidationException.alreadyExists("username", effectiveUsername);
        }
        
        // Create user entity
        User user = userMapper.toEntity(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        
        // Generate email verification token
        String verificationToken = UUID.randomUUID().toString();
        user.generateEmailVerificationToken(verificationToken, 24); // 24 hours validity
        
        // Auto-verify email for development purposes
        user.verifyEmail();
        
        // Save user
        User savedUser = userRepository.save(user);
        
        // Send verification email — wrapped in try-catch so that a missing/misconfigured
        // mail server never prevents a successful registration.
        try {
            String verificationLink = frontendUrl + "/verify-email?token=" + verificationToken;
            emailService.sendWelcomeEmail(savedUser.getEmail(),
                    savedUser.getFirstName() + " " + savedUser.getLastName(),
                    verificationLink);
        } catch (Exception emailEx) {
            log.warn("Welcome email could not be sent to {} (registration still succeeded): {}",
                    savedUser.getEmail(), emailEx.getMessage());
        }
        
        log.info("User registered successfully with ID: {}", savedUser.getId());
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(UUID userId) {
        ValidationUtil.requireNotNull(userId, "userId");
        return userRepository.findById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        ValidationUtil.requireNotBlank(email, "email");
        return userRepository.findByEmail(email.trim().toLowerCase());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        ValidationUtil.requireNotBlank(username, "username");
        return userRepository.findByUsername(username.trim().toLowerCase());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmailOrUsername(String identifier) {
        ValidationUtil.requireNotBlank(identifier, "identifier");
        return userRepository.findByEmailOrUsername(identifier.trim().toLowerCase());
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(UUID userId) {
        return findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.user(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return findByEmail(email)
                .orElseThrow(() -> ResourceNotFoundException.userByEmail(email));
    }

    @Override
    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        log.info("Updating profile for user: {}", userId);
        
        User user = getUserById(userId);
        request.sanitize();
        
        // Validate and update fields if provided
        if (StringUtil.isNotBlank(request.getUsername())) {
            if (!isUsernameAvailableForUser(request.getUsername(), userId)) {
                throw ValidationException.alreadyExists("username", request.getUsername());
            }
            user.setUsername(request.getUsername());
        }
        
        if (StringUtil.isNotBlank(request.getFirstName())) {
            user.setFirstName(request.getFirstName());
        }
        
        if (StringUtil.isNotBlank(request.getLastName())) {
            user.setLastName(request.getLastName());
        }
        
        if (StringUtil.isNotBlank(request.getPhone())) {
            ValidationUtil.validatePhoneNumber(request.getPhone(), "phone");
            user.setPhone(request.getPhone());
        }
        
        if (StringUtil.isNotBlank(request.getBio())) {
            user.setBio(request.getBio());
        }
        
        if (StringUtil.isNotBlank(request.getTimezone())) {
            user.setTimezone(request.getTimezone());
        }
        
        if (StringUtil.isNotBlank(request.getLanguage())) {
            user.setLanguage(request.getLanguage());
        }
        
        if (request.getMarketingEmailsEnabled() != null) {
            user.setMarketingEmailsEnabled(request.getMarketingEmailsEnabled());
        }
        
        if (request.getNotificationEmailsEnabled() != null) {
            user.setNotificationEmailsEnabled(request.getNotificationEmailsEnabled());
        }
        
        User savedUser = userRepository.save(user);
        log.info("Profile updated successfully for user: {}", userId);
        
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        log.info("Changing password for user: {}", userId);
        
        User user = getUserById(userId);
        
        // Validate current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new ValidationException("Current password is incorrect");
        }
        
        // Validate new password format and confirmation
        if (!request.passwordsMatch()) {
            throw new ValidationException("Password confirmation does not match");
        }
        
        if (!request.isNewPasswordDifferent()) {
            throw new ValidationException("New password must be different from current password");
        }
        
        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        log.info("Password changed successfully for user: {}", userId);
    }

    @Override
    @Transactional
    public UserResponse verifyEmail(String token) {
        log.info("Verifying email with token");
        
        ValidationUtil.requireNotBlank(token, "token");
        
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new ValidationException("Invalid or expired verification token"));
        
        if (!user.isEmailVerificationTokenValid(token)) {
            throw new ValidationException("Verification token has expired");
        }
        
        user.verifyEmail();
        User savedUser = userRepository.save(user);
        
        log.info("Email verified successfully for user: {}", savedUser.getId());
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public void resendEmailVerification(UUID userId) {
        log.info("Resending email verification for user: {}", userId);
        
        User user = getUserById(userId);
        
        if (user.getEmailVerified()) {
            throw new ValidationException("Email is already verified");
        }
        
        // Generate new verification token
        String verificationToken = UUID.randomUUID().toString();
        user.generateEmailVerificationToken(verificationToken, 24);
        
        userRepository.save(user);
        
        // Send verification email
        // emailService.sendVerificationEmail(user.getEmail(), verificationToken);
        
        log.info("Email verification resent for user: {}", userId);
    }

    @Override
    @Transactional
    public void generatePasswordResetToken(String email) {
        log.info("Generating password reset token for email: {}", StringUtil.maskEmail(email));
        
        Optional<User> userOpt = findByEmail(email);
        if (userOpt.isEmpty()) {
            // For security, don't reveal that email doesn't exist
            log.warn("Password reset requested for non-existent email: {}", StringUtil.maskEmail(email));
            return;
        }
        
        User user = userOpt.get();
        String resetToken = UUID.randomUUID().toString();
        user.generatePasswordResetToken(resetToken, 1); // 1 hour validity
        
        userRepository.save(user);
        
        // Send password reset email
        String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
        emailService.sendPasswordResetEmail(user.getEmail(), 
                user.getFirstName() + " " + user.getLastName(), 
                resetLink);
        
        log.info("Password reset token generated for user: {}", user.getId());
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        log.info("Resetting password with token");
        
        ValidationUtil.requireNotBlank(token, "token");
        ValidationUtil.requireNotBlank(newPassword, "newPassword");
        
        User user = userRepository.findByPasswordResetToken(token)
                .orElseThrow(() -> new ValidationException("Invalid or expired reset token"));
        
        if (!user.isPasswordResetTokenValid(token)) {
            throw new ValidationException("Password reset token has expired");
        }
        
        // Update password and clear reset token
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.clearPasswordResetToken();
        
        userRepository.save(user);
        
        log.info("Password reset successfully for user: {}", user.getId());
    }

    @Override
    @Transactional
    public void updateLastLogin(UUID userId) {
        log.debug("Updating last login for user: {}", userId);
        
        User user = getUserById(userId);
        user.resetLoginAttempts(); // This also updates lastLoginAt
        
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void handleFailedLogin(UUID userId) {
        log.warn("Handling failed login for user: {}", userId);
        
        User user = getUserById(userId);
        user.incrementLoginAttempts();
        
        // Lock account if max attempts reached
        if (user.getLoginAttempts() >= maxLoginAttempts) {
            user.lockAccount(lockoutDurationMinutes);
            log.warn("User account locked due to too many failed attempts: {}", userId);
        }
        
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void lockUser(UUID userId, int minutes) {
        log.info("Locking user account: {} for {} minutes", userId, minutes);
        
        User user = getUserById(userId);
        user.lockAccount(minutes);
        
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void unlockUser(UUID userId) {
        log.info("Unlocking user account: {}", userId);
        
        User user = getUserById(userId);
        user.unlockAccount();
        
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void activateUser(UUID userId) {
        log.info("Activating user account: {}", userId);
        
        User user = getUserById(userId);
        user.activate();
        
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deactivateUser(UUID userId) {
        log.info("Deactivating user account: {}", userId);
        
        User user = getUserById(userId);
        user.deactivate();
        
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        log.info("Soft deleting user account: {}", userId);
        
        User user = getUserById(userId);
        user.softDelete();
        
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateProfilePicture(UUID userId, String pictureUrl) {
        log.info("Updating profile picture for user: {}", userId);
        
        ValidationUtil.requireNotBlank(pictureUrl, "pictureUrl");
        
        User user = getUserById(userId);
        user.setProfilePictureUrl(pictureUrl);
        
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        ValidationUtil.requireNotBlank(email, "email");
        return !userRepository.existsByEmail(email.trim().toLowerCase());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(String username) {
        if (StringUtil.isBlank(username)) {
            return true; // Username is optional
        }
        return !userRepository.existsByUsername(username.trim().toLowerCase());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailAvailableForUser(String email, UUID userId) {
        ValidationUtil.requireNotBlank(email, "email");
        ValidationUtil.requireNotNull(userId, "userId");
        return !userRepository.existsByEmailAndIdNot(email.trim().toLowerCase(), userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isUsernameAvailableForUser(String username, UUID userId) {
        if (StringUtil.isBlank(username)) {
            return true;
        }
        ValidationUtil.requireNotNull(userId, "userId");
        return !userRepository.existsByUsernameAndIdNot(username.trim().toLowerCase(), userId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> searchUsers(String search, Pageable pageable) {
        ValidationUtil.requireNotBlank(search, "search");
        
        Page<User> users = userRepository.searchUsers(search.trim(), pageable);
        Page<UserResponse> userResponses = users.map(userMapper::toBasicResponse);
        
        return PageResponse.from(userResponses);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getActiveUsers(Pageable pageable) {
        Page<User> users = userRepository.findActiveUsers(pageable);
        Page<UserResponse> userResponses = users.map(userMapper::toBasicResponse);
        
        return PageResponse.from(userResponses);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAllForAdmin(pageable);
        Page<UserResponse> userResponses = users.map(userMapper::toAdminResponse);
        
        return PageResponse.from(userResponses);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(User.UserRole role) {
        ValidationUtil.requireNotNull(role, "role");
        
        List<User> users = userRepository.findByRole(role);
        return users.stream()
                .map(userMapper::toBasicResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserStatistics getUserStatistics() {
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime tomorrowStart = todayStart.plusDays(1);
        LocalDateTime now = LocalDateTime.now();
        
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countActiveUsers();
        long verifiedUsers = userRepository.countVerifiedUsers();
        long usersRegisteredToday = userRepository.countUsersRegisteredToday(todayStart, tomorrowStart);
        long lockedUsers = userRepository.findLockedUsers(now).size();
        
        return new UserStatistics(totalUsers, activeUsers, verifiedUsers, usersRegisteredToday, lockedUsers);
    }

    @Override
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Cleaning up expired tokens");
        
        LocalDateTime now = LocalDateTime.now();
        
        int expiredEmailTokens = userRepository.cleanExpiredEmailVerificationTokens(now);
        int expiredPasswordTokens = userRepository.cleanExpiredPasswordResetTokens(now);
        
        log.info("Cleaned up {} expired email verification tokens and {} expired password reset tokens",
                expiredEmailTokens, expiredPasswordTokens);
    }

    /**
     * Validate registration request.
     */
    private void validateRegistrationRequest(RegisterRequest request) {
        ValidationUtil.validateEmail(request.getEmail(), "email");
        ValidationUtil.requireNotBlank(request.getPassword(), "password");
        ValidationUtil.requireNotBlank(request.getFirstName(), "firstName");
        ValidationUtil.requireNotBlank(request.getLastName(), "lastName");
        
        if (!request.passwordsMatch()) {
            throw new ValidationException("Password confirmation does not match");
        }
        
        if (!request.hasAcceptedRequiredTerms()) {
            throw new ValidationException("You must accept the terms and conditions and privacy policy");
        }
        
        if (StringUtil.isNotBlank(request.getPhone())) {
            ValidationUtil.validatePhoneNumber(request.getPhone(), "phone");
        }
    }
}