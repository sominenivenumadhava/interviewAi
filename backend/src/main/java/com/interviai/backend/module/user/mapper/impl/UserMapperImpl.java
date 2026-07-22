package com.interviai.backend.module.user.mapper.impl;

import com.interviai.backend.module.user.dto.request.RegisterRequest;
import com.interviai.backend.module.user.dto.response.UserResponse;
import com.interviai.backend.module.user.entity.User;
import com.interviai.backend.module.user.mapper.UserMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Manual implementation of UserMapper to avoid MapStruct issues with inheritance.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User toEntity(RegisterRequest request) {
        if (request == null) {
            return null;
        }

        User user = User.builder()
                .email(request.getEmail())
                .username(request.getEffectiveUsername())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .timezone(request.getEffectiveTimezone())
                .language(request.getEffectiveLanguage())
                .marketingEmailsEnabled(request.getMarketingEmails())
                .notificationEmailsEnabled(request.getNotificationEmails())
                .emailVerified(false)
                .role(User.UserRole.USER)
                .loginAttempts(0)
                .termsAcceptedAt(LocalDateTime.now())
                .privacyAcceptedAt(LocalDateTime.now())
                .build();
        
        // Set inherited fields manually since they're private
        user.activate(); // Sets isActive = true
        // isDeleted defaults to false in the entity
        
        return user;
    }

    @Override
    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .displayName(user.getDisplayName())
                .phone(user.getPhone())
                .profilePictureUrl(user.getProfilePictureUrl())
                .bio(user.getBio())
                .timezone(user.getTimezone())
                .language(user.getLanguage())
                .role(user.getRole())
                .emailVerified(user.getEmailVerified())
                .isActive(user.getIsActive())
                .isLocked(user.isAccountLocked())
                .profileCompleteness(user.getProfileCompleteness())
                .marketingEmailsEnabled(user.getMarketingEmailsEnabled())
                .notificationEmailsEnabled(user.getNotificationEmailsEnabled())
                .lastLoginAt(user.getLastLoginAt())
                .termsAcceptedAt(user.getTermsAcceptedAt())
                .privacyAcceptedAt(user.getPrivacyAcceptedAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    @Override
    public UserResponse toBasicResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .displayName(user.getDisplayName())
                .profilePictureUrl(user.getProfilePictureUrl())
                .emailVerified(user.getEmailVerified())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Override
    public UserResponse toPublicResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .displayName(user.getDisplayName())
                .profilePictureUrl(user.getProfilePictureUrl())
                .bio(user.getBio())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Override
    public UserResponse toAdminResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .displayName(user.getDisplayName())
                .phone(user.getPhone())
                .profilePictureUrl(user.getProfilePictureUrl())
                .bio(user.getBio())
                .timezone(user.getTimezone())
                .language(user.getLanguage())
                .role(user.getRole())
                .emailVerified(user.getEmailVerified())
                .isActive(user.getIsActive())
                .isLocked(user.isAccountLocked())
                .profileCompleteness(user.getProfileCompleteness())
                .marketingEmailsEnabled(user.getMarketingEmailsEnabled())
                .notificationEmailsEnabled(user.getNotificationEmailsEnabled())
                .lastLoginAt(user.getLastLoginAt())
                .termsAcceptedAt(user.getTermsAcceptedAt())
                .privacyAcceptedAt(user.getPrivacyAcceptedAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    @Override
    public String getFullName(User user) {
        if (user == null) {
            return null;
        }
        return user.getFullName();
    }

    @Override
    public String getDisplayName(User user) {
        if (user == null) {
            return null;
        }
        return user.getDisplayName();
    }
}