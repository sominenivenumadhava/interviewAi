package com.interviai.backend.module.user.controller;

import com.interviai.backend.common.constant.ApiConstants;
import com.interviai.backend.common.dto.ApiResponse;
import com.interviai.backend.common.dto.PageResponse;
import com.interviai.backend.module.user.dto.request.*;
import com.interviai.backend.module.user.dto.response.UserResponse;
import com.interviai.backend.module.user.entity.User;
import com.interviai.backend.module.user.mapper.UserMapper;
import com.interviai.backend.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for user management operations.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping(ApiConstants.USERS_BASE_PATH)
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User registration, profile management, and user operations")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @Operation(
        summary = "Register new user",
        description = "Register a new user account with email verification"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "User registered successfully",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid registration data or email already exists"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Email or username already exists"
        )
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("User registration request received for email: {}", request.getEmail());
        
        UserResponse userResponse = userService.registerUser(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                    userResponse,
                    "User registered successfully. Please check your email to verify your account."
                ));
    }

    @Operation(
        summary = "Get current user profile",
        description = "Retrieve the authenticated user's profile information",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Profile retrieved successfully",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "User not authenticated"
        )
    })
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUserProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = (User) userDetails;
        User foundUser = userService.getUserById(user.getId());
        UserResponse userResponse = userMapper.toResponse(foundUser);
        
        return ResponseEntity.ok(ApiResponse.success(
            userResponse,
            "Profile retrieved successfully"
        ));
    }

    @Operation(
        summary = "Update user profile",
        description = "Update the authenticated user's profile information",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Profile updated successfully",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid profile data"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "User not authenticated"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Username already exists"
        )
    })
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = (User) userDetails;
        log.info("Profile update request for user: {}", user.getId());
        
        UserResponse userResponse = userService.updateProfile(user.getId(), request);
        
        return ResponseEntity.ok(ApiResponse.success(
            userResponse,
            "Profile updated successfully"
        ));
    }

    @Operation(
        summary = "Change user password",
        description = "Change the authenticated user's password",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Password changed successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid password data or current password is incorrect"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "User not authenticated"
        )
    })
    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = (User) userDetails;
        log.info("Password change request for user: {}", user.getId());
        
        userService.changePassword(user.getId(), request);
        
        return ResponseEntity.ok(ApiResponse.<Void>success("Password changed successfully"));
    }

    @Operation(
        summary = "Verify email address",
        description = "Verify user's email address using verification token"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Email verified successfully",
            content = @Content(schema = @Schema(implementation = UserResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid or expired verification token"
        )
    })
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<UserResponse>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request) {
        
        log.info("Email verification request received");
        
        UserResponse userResponse = userService.verifyEmail(request.getToken());
        
        return ResponseEntity.ok(ApiResponse.success(
            userResponse,
            "Email verified successfully"
        ));
    }

    @Operation(
        summary = "Resend email verification",
        description = "Resend email verification token to the authenticated user",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Verification email sent successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Email is already verified"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "User not authenticated"
        )
    })
    @PostMapping("/resend-verification")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> resendEmailVerification(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = (User) userDetails;
        log.info("Resend email verification request for user: {}", user.getId());
        
        userService.resendEmailVerification(user.getId());
        
        return ResponseEntity.ok(ApiResponse.<Void>success("Verification email sent successfully"));
    }

    @Operation(
        summary = "Check email availability",
        description = "Check if an email address is available for registration"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Email availability checked"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid email format"
        )
    })
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailAvailability(
            @RequestParam @Email @NotBlank String email) {
        
        boolean available = userService.isEmailAvailable(email);
        
        return ResponseEntity.ok(ApiResponse.success(
            available,
            available ? "Email is available" : "Email is already taken"
        ));
    }

    @Operation(
        summary = "Check username availability",
        description = "Check if a username is available for registration"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Username availability checked"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid username format"
        )
    })
    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<Boolean>> checkUsernameAvailability(
            @RequestParam @NotBlank String username) {
        
        boolean available = userService.isUsernameAvailable(username);
        
        return ResponseEntity.ok(ApiResponse.success(
            available,
            available ? "Username is available" : "Username is already taken"
        ));
    }

    @Operation(
        summary = "Update profile picture",
        description = "Update the authenticated user's profile picture",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Profile picture updated successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid picture URL"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "User not authenticated"
        )
    })
    @PostMapping("/profile-picture")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> updateProfilePicture(
            @RequestParam @NotBlank String pictureUrl,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        User user = (User) userDetails;
        log.info("Profile picture update request for user: {}", user.getId());
        
        userService.updateProfilePicture(user.getId(), pictureUrl);
        
        return ResponseEntity.ok(ApiResponse.<Void>success("Profile picture updated successfully"));
    }

    // Admin endpoints
    @Operation(
        summary = "Search users (Admin only)",
        description = "Search users by name, email, or username",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Users retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access denied - Admin role required"
        )
    })
    @GetMapping("/admin/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> searchUsers(
            @RequestParam @NotBlank String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        PageResponse<UserResponse> users = userService.searchUsers(search, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(
            users,
            "Users retrieved successfully"
        ));
    }

    @Operation(
        summary = "Get all users (Admin only)",
        description = "Get all users with admin-level information",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Users retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access denied - Admin role required"
        )
    })
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        PageResponse<UserResponse> users = userService.getAllUsers(pageable);
        
        return ResponseEntity.ok(ApiResponse.success(
            users,
            "All users retrieved successfully"
        ));
    }

    @Operation(
        summary = "Get active users (Admin only)",
        description = "Get all active users",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Active users retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access denied - Admin role required"
        )
    })
    @GetMapping("/admin/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getActiveUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        PageResponse<UserResponse> users = userService.getActiveUsers(pageable);
        
        return ResponseEntity.ok(ApiResponse.success(
            users,
            "Active users retrieved successfully"
        ));
    }

    @Operation(
        summary = "Get users by role (Admin only)",
        description = "Get users by specific role",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Users retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access denied - Admin role required"
        )
    })
    @GetMapping("/admin/by-role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(
            @PathVariable User.UserRole role) {
        
        List<UserResponse> users = userService.getUsersByRole(role);
        
        return ResponseEntity.ok(ApiResponse.success(
            users,
            String.format("Users with role %s retrieved successfully", role)
        ));
    }

    @Operation(
        summary = "Get user statistics (Admin only)",
        description = "Get comprehensive user statistics",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Statistics retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access denied - Admin role required"
        )
    })
    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserService.UserStatistics>> getUserStatistics() {
        
        UserService.UserStatistics statistics = userService.getUserStatistics();
        
        return ResponseEntity.ok(ApiResponse.success(
            statistics,
            "User statistics retrieved successfully"
        ));
    }

    @Operation(
        summary = "Lock user account (Admin only)",
        description = "Lock a user account for specified duration",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "User account locked successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access denied - Admin role required"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "User not found"
        )
    })
    @PostMapping("/admin/{userId}/lock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> lockUser(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "30") int minutes) {
        
        log.info("Admin request to lock user: {} for {} minutes", userId, minutes);
        
        userService.lockUser(userId, minutes);
        
        return ResponseEntity.ok(ApiResponse.<Void>success(
            String.format("User locked for %d minutes", minutes)
        ));
    }

    @Operation(
        summary = "Unlock user account (Admin only)",
        description = "Unlock a user account",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "User account unlocked successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access denied - Admin role required"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "User not found"
        )
    })
    @PostMapping("/admin/{userId}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> unlockUser(@PathVariable UUID userId) {
        
        log.info("Admin request to unlock user: {}", userId);
        
        userService.unlockUser(userId);
        
        return ResponseEntity.ok(ApiResponse.<Void>success(
            "User account unlocked successfully"
        ));
    }

    @Operation(
        summary = "Activate user account (Admin only)",
        description = "Activate a deactivated user account",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "User account activated successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access denied - Admin role required"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "User not found"
        )
    })
    @PostMapping("/admin/{userId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable UUID userId) {
        
        log.info("Admin request to activate user: {}", userId);
        
        userService.activateUser(userId);
        
        return ResponseEntity.ok(ApiResponse.<Void>success(
            "User account activated successfully"
        ));
    }

    @Operation(
        summary = "Deactivate user account (Admin only)",
        description = "Deactivate a user account",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "User account deactivated successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access denied - Admin role required"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "User not found"
        )
    })
    @PostMapping("/admin/{userId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable UUID userId) {
        
        log.info("Admin request to deactivate user: {}", userId);
        
        userService.deactivateUser(userId);
        
        return ResponseEntity.ok(ApiResponse.<Void>success(
            "User account deactivated successfully"
        ));
    }

    @Operation(
        summary = "Delete user account (Admin only)",
        description = "Soft delete a user account",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "User account deleted successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Access denied - Admin role required"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "User not found"
        )
    })
    @DeleteMapping("/admin/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID userId) {
        
        log.info("Admin request to delete user: {}", userId);
        
        userService.deleteUser(userId);
        
        return ResponseEntity.ok(ApiResponse.<Void>success(
            "User account deleted successfully"
        ));
    }
}