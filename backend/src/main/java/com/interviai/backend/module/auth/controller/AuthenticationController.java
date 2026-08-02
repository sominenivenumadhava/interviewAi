package com.interviai.backend.module.auth.controller;

import com.interviai.backend.common.constant.ApiConstants;
import com.interviai.backend.common.dto.ApiResponse;
import com.interviai.backend.module.auth.dto.request.*;
import com.interviai.backend.module.auth.dto.response.*;
import com.interviai.backend.module.auth.service.AuthenticationService;
import com.interviai.backend.module.user.entity.User;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for authentication operations.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping(ApiConstants.AUTH_BASE_PATH)
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and authorization operations")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final UserService userService;

    @Operation(
        summary = "User login",
        description = "Authenticate user credentials and return JWT tokens"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Invalid credentials"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request data"
        )
    })
    @PostMapping(ApiConstants.LOGIN_ENDPOINT)
    public ResponseEntity<ApiResponse<AuthenticationResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("Login request received from IP: {}", getClientIpAddress(httpRequest));
        
        String ipAddress = getClientIpAddress(httpRequest);
        AuthenticationResponse response = authenticationService.authenticate(request, ipAddress);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @Operation(
        summary = "Refresh access token",
        description = "Generate new access token using refresh token"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Token refresh successful",
            content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Invalid or expired refresh token"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request data"
        )
    })
    @PostMapping(ApiConstants.REFRESH_TOKEN_ENDPOINT)
    public ResponseEntity<ApiResponse<AuthenticationResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        
        log.debug("Token refresh request received from IP: {}", getClientIpAddress(httpRequest));
        
        String ipAddress = getClientIpAddress(httpRequest);
        AuthenticationResponse response = authenticationService.refreshToken(request, ipAddress);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Token refresh successful"));
    }

    @Operation(
        summary = "User logout",
        description = "Logout user and revoke refresh tokens"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Logout successful",
            content = @Content(schema = @Schema(implementation = LogoutResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        )
    })
    @PostMapping(ApiConstants.LOGOUT_ENDPOINT)
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<LogoutResponse>> logout(
            @RequestBody(required = false) LogoutRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
        
        log.info("Logout request received for user: {}", userDetails.getUsername());
        
        // Extract real user ID from the JWT token
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        UUID userId = authenticationService.extractUserId(token);
        
        LogoutRequest logoutRequest = request != null ? request : LogoutRequest.builder().build();
        LogoutResponse response = authenticationService.logout(logoutRequest, userId);
        
        return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
    }

    @Operation(
        summary = "Forgot password",
        description = "Initiate password reset process by sending reset email"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Password reset email sent",
            content = @Content(schema = @Schema(implementation = PasswordResetResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid email format"
        )
    })
    @PostMapping(ApiConstants.FORGOT_PASSWORD_ENDPOINT)
    public ResponseEntity<ApiResponse<PasswordResetResponse>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        
        log.info("Forgot password request received");
        
        PasswordResetResponse response = authenticationService.forgotPassword(request);
        
        return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
    }

    @Operation(
        summary = "Reset password",
        description = "Reset user password using reset token"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Password reset successful",
            content = @Content(schema = @Schema(implementation = PasswordResetResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid token or password format"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Invalid or expired reset token"
        )
    })
    @PostMapping(ApiConstants.RESET_PASSWORD_ENDPOINT)
    public ResponseEntity<ApiResponse<PasswordResetResponse>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        
        log.info("Password reset request received");
        
        PasswordResetResponse response = authenticationService.resetPassword(request);
        
        return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
    }

    @Operation(
        summary = "Validate token",
        description = "Validate JWT access token (for debugging purposes)"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Token is valid"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Token is invalid or expired"
        )
    })
    @GetMapping("/validate")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<String>> validateToken(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.debug("Token validation request for user: {}", userDetails.getUsername());
        
        return ResponseEntity.ok(ApiResponse.success("Token is valid", "Token validation successful"));
    }

    @Operation(
        summary = "Get current user info",
        description = "Get authenticated user information from token"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "User information retrieved"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        )
    })
    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<AuthenticationResponse.UserInfo>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(hidden = true) @RequestHeader("Authorization") String authHeader) {
        
        log.debug("Current user info request for: {}", userDetails.getUsername());
        
        // Extract token and get user info
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        UUID userId = authenticationService.extractUserId(token);
        
        // Fetch real user data from UserService
        User user = userService.getUserById(userId);
        
        AuthenticationResponse.UserInfo userInfo = AuthenticationResponse.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .emailVerified(user.getEmailVerified())
                .isActive(user.getIsActive())
                .profileCompleteness(user.getProfileCompleteness())
                .role(user.getRole() != null ? user.getRole().name() : "USER")
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(userInfo, "User information retrieved"));
    }

    /**
     * Extract client IP address from request.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader != null && !xForwardedForHeader.isEmpty()) {
            return xForwardedForHeader.split(",")[0].trim();
        }
        
        String xRealIpHeader = request.getHeader("X-Real-IP");
        if (xRealIpHeader != null && !xRealIpHeader.isEmpty()) {
            return xRealIpHeader;
        }
        
        return request.getRemoteAddr();
    }
}