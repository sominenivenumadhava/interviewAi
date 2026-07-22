package com.interviai.backend.module.user.service.impl;

import com.interviai.backend.module.user.entity.User;
import com.interviai.backend.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Custom UserDetailsService implementation for Spring Security.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Slf4j
@Service("userDetailsService")
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    /**
     * Load user by username (email in our case).
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);
        
        try {
            // Try to find by email first, then by username
            User user = userService.findByEmail(username)
                    .or(() -> userService.findByUsername(username))
                    .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("User not found with email or username: %s", username)
                    ));
            
            // Check if account is active
            if (!user.getIsActive()) {
                throw new UsernameNotFoundException("User account is deactivated");
            }
            
            // Check if account is deleted
            if (user.getIsDeleted()) {
                throw new UsernameNotFoundException("User account is deleted");
            }
            
            // Check if account is locked
            if (user.isAccountLocked()) {
                log.warn("Attempted login to locked account: {}", username);
                // Note: We still return the user so Spring Security can handle the locked account
                // The actual locking logic should be handled in a custom authentication provider
            }
            
            log.debug("Successfully loaded user: {} with role: {}", user.getEmail(), user.getRole());
            return user; // User entity implements UserDetails
            
        } catch (UsernameNotFoundException e) {
            log.debug("User not found: {}", username);
            throw e;
        } catch (Exception e) {
            log.error("Error loading user by username: {} - {}", username, e.getMessage());
            throw new UsernameNotFoundException("Error loading user: " + e.getMessage(), e);
        }
    }
    
    /**
     * Load user by user ID.
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(String userId) throws UsernameNotFoundException {
        log.debug("Loading user by ID: {}", userId);
        
        try {
            java.util.UUID id = java.util.UUID.fromString(userId);
            User user = userService.getUserById(id);
            
            // Check if account is active
            if (!user.getIsActive()) {
                throw new UsernameNotFoundException("User account is deactivated");
            }
            
            // Check if account is deleted
            if (user.getIsDeleted()) {
                throw new UsernameNotFoundException("User account is deleted");
            }
            
            log.debug("Successfully loaded user by ID: {} - {}", userId, user.getEmail());
            return user; // User entity implements UserDetails
            
        } catch (IllegalArgumentException e) {
            log.debug("Invalid user ID format: {}", userId);
            throw new UsernameNotFoundException("Invalid user ID format");
        } catch (Exception e) {
            log.error("Error loading user by ID: {} - {}", userId, e.getMessage());
            throw new UsernameNotFoundException("User not found with ID: " + userId, e);
        }
    }
    
    /**
     * Check if user account is locked.
     */
    public boolean isAccountLocked(String username) {
        try {
            User user = userService.findByEmailOrUsername(username).orElse(null);
            return user != null && user.isAccountLocked();
        } catch (Exception e) {
            log.warn("Error checking account lock status for: {} - {}", username, e.getMessage());
            return false;
        }
    }
    
    /**
     * Handle failed authentication attempt.
     */
    public void handleFailedAuthentication(String username) {
        try {
            User user = userService.findByEmailOrUsername(username).orElse(null);
            if (user != null) {
                userService.handleFailedLogin(user.getId());
                log.debug("Handled failed authentication for user: {}", user.getId());
            }
        } catch (Exception e) {
            log.warn("Error handling failed authentication for: {} - {}", username, e.getMessage());
        }
    }
    
    /**
     * Handle successful authentication attempt.
     */
    public void handleSuccessfulAuthentication(String username) {
        try {
            User user = userService.findByEmailOrUsername(username).orElse(null);
            if (user != null) {
                userService.updateLastLogin(user.getId());
                log.debug("Handled successful authentication for user: {}", user.getId());
            }
        } catch (Exception e) {
            log.warn("Error handling successful authentication for: {} - {}", username, e.getMessage());
        }
    }
}