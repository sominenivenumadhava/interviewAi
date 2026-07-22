package com.interviai.backend.security;

import com.interviai.backend.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Custom UserDetailsService implementation for loading user-specific data.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Slf4j
@Primary
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);
        
        try {
            // Look up user by email or username
            Optional<com.interviai.backend.module.user.entity.User> userOptional = 
                userService.findByEmailOrUsername(username);
            
            if (userOptional.isEmpty()) {
                log.warn("User not found: {}", username);
                throw new UsernameNotFoundException("User not found: " + username);
            }
            
            com.interviai.backend.module.user.entity.User user = userOptional.get();
            
            // User entity implements UserDetails interface directly
            return user;
            
        } catch (UsernameNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error loading user: {} - {}", username, e.getMessage());
            throw new UsernameNotFoundException("Error loading user: " + username);
        }
    }

    /**
     * Load user by user ID (for JWT token validation).
     */
    public UserDetails loadUserById(String userId) throws UsernameNotFoundException {
        log.debug("Loading user by ID: {}", userId);
        
        try {
            UUID userUUID = UUID.fromString(userId);
            Optional<com.interviai.backend.module.user.entity.User> userOptional = 
                userService.findById(userUUID);
            
            if (userOptional.isEmpty()) {
                log.warn("User not found with ID: {}", userId);
                throw new UsernameNotFoundException("User not found with ID: " + userId);
            }
            
            // User entity implements UserDetails interface directly
            return userOptional.get();
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid user ID format: {}", userId);
            throw new UsernameNotFoundException("Invalid user ID format: " + userId);
        } catch (Exception e) {
            log.error("Error loading user by ID: {} - {}", userId, e.getMessage());
            throw new UsernameNotFoundException("User not found with ID: " + userId);
        }
    }

    /**
     * Check if user account is enabled and verified.
     */
    public boolean isUserAccountValid(String username) {
        try {
            UserDetails userDetails = loadUserByUsername(username);
            return userDetails.isEnabled() && 
                   userDetails.isAccountNonLocked() && 
                   userDetails.isAccountNonExpired() && 
                   userDetails.isCredentialsNonExpired();
        } catch (UsernameNotFoundException e) {
            return false;
        }
    }

    /**
     * Get user authorities as string list.
     */
    public java.util.List<String> getUserAuthorities(String username) {
        try {
            UserDetails userDetails = loadUserByUsername(username);
            return userDetails.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .toList();
        } catch (UsernameNotFoundException e) {
            return java.util.Collections.emptyList();
        }
    }
}