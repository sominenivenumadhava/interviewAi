package com.interviai.backend.module.user.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.interviai.backend.module.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for user information.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "User information response")
public class UserResponse {

    @Schema(description = "User ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Email address", example = "user@example.com")
    private String email;

    @Schema(description = "Username", example = "john_doe")
    private String username;

    @Schema(description = "First name", example = "John")
    private String firstName;

    @Schema(description = "Last name", example = "Doe")
    private String lastName;

    @Schema(description = "Full name", example = "John Doe")
    private String fullName;

    @Schema(description = "Display name", example = "John")
    private String displayName;

    @Schema(description = "Phone number", example = "+1234567890")
    private String phone;

    @Schema(description = "Profile picture URL", example = "https://example.com/images/profile.jpg")
    private String profilePictureUrl;

    @Schema(description = "User biography", example = "Software engineer with 5+ years of experience")
    private String bio;

    @Schema(description = "User timezone", example = "America/New_York")
    private String timezone;

    @Schema(description = "User language", example = "en")
    private String language;

    @Schema(description = "User role", example = "USER")
    private User.UserRole role;

    @Schema(description = "Email verification status", example = "true")
    private Boolean emailVerified;

    @Schema(description = "Account active status", example = "true")
    private Boolean isActive;

    @Schema(description = "Account locked status", example = "false")
    private Boolean isLocked;

    @Schema(description = "Profile completeness percentage", example = "85")
    private Integer profileCompleteness;

    @Schema(description = "Marketing emails enabled", example = "true")
    private Boolean marketingEmailsEnabled;

    @Schema(description = "Notification emails enabled", example = "true")
    private Boolean notificationEmailsEnabled;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Schema(description = "Last login timestamp")
    private LocalDateTime lastLoginAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Schema(description = "Terms acceptance timestamp")
    private LocalDateTime termsAcceptedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Schema(description = "Privacy policy acceptance timestamp")
    private LocalDateTime privacyAcceptedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Schema(description = "Account creation timestamp")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @Schema(description = "Account last update timestamp")
    private LocalDateTime updatedAt;

    /**
     * Create public profile response (limited information).
     */
    public static UserResponse createPublicProfile(User user) {
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

    /**
     * Create basic user response (minimal information).
     */
    public static UserResponse createBasic(User user) {
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
                .build();
    }

    /**
     * Create admin view response (comprehensive information).
     */
    public static UserResponse createAdminView(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFullName())
                .displayName(user.getDisplayName())
                .phone(user.getPhone())
                .role(user.getRole())
                .emailVerified(user.getEmailVerified())
                .isActive(user.getIsActive())
                .isLocked(user.isAccountLocked())
                .profileCompleteness(user.getProfileCompleteness())
                .lastLoginAt(user.getLastLoginAt())
                .termsAcceptedAt(user.getTermsAcceptedAt())
                .privacyAcceptedAt(user.getPrivacyAcceptedAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}