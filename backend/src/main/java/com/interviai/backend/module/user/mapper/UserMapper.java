package com.interviai.backend.module.user.mapper;

import com.interviai.backend.module.user.dto.request.RegisterRequest;
import com.interviai.backend.module.user.dto.response.UserResponse;
import com.interviai.backend.module.user.entity.User;

/**
 * Mapper interface for User-related DTOs and entities.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
public interface UserMapper {

    /**
     * Map RegisterRequest to User entity.
     */
    User toEntity(RegisterRequest request);

    /**
     * Map User entity to UserResponse.
     */
    UserResponse toResponse(User user);

    /**
     * Map User entity to basic UserResponse.
     */
    UserResponse toBasicResponse(User user);

    /**
     * Map User entity to public profile response.
     */
    UserResponse toPublicResponse(User user);

    /**
     * Map User entity to admin response (full details).
     */
    UserResponse toAdminResponse(User user);

    /**
     * Helper method to get full name.
     */
    String getFullName(User user);

    /**
     * Helper method to get display name.
     */
    String getDisplayName(User user);
}