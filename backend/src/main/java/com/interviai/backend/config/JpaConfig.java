package com.interviai.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA configuration for the application.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.interviai.backend")
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@EnableTransactionManagement
public class JpaConfig {

    /**
     * Auditor provider for JPA auditing.
     */
    @Bean
    public AuditorAware<UUID> auditorProvider() {
        return new SpringSecurityAuditorAware();
    }

    /**
     * Spring Security auditor aware implementation.
     */
    public static class SpringSecurityAuditorAware implements AuditorAware<UUID> {

        @Override
        public Optional<UUID> getCurrentAuditor() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || 
                !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.empty();
            }

            try {
                // Check if principal is our User entity (which implements UserDetails)
                if (authentication.getPrincipal() instanceof com.interviai.backend.module.user.entity.User user) {
                    return Optional.of(user.getId());
                }
                
                // Fallback - try to parse from username if it's a UUID
                Object principal = authentication.getPrincipal();
                if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
                    String username = userDetails.getUsername();
                    // If username is actually a UUID (which might happen in some auth scenarios)
                    try {
                        return Optional.of(UUID.fromString(username));
                    } catch (IllegalArgumentException e) {
                        // Username is not a UUID, that's fine
                    }
                }
                
                return Optional.empty();
                
            } catch (Exception e) {
                // Fallback to empty if user ID extraction fails
                return Optional.empty();
            }
        }
    }
}