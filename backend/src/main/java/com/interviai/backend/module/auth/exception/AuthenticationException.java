package com.interviai.backend.module.auth.exception;

import com.interviai.backend.common.exception.BusinessException;

/**
 * Exception for authentication-related errors.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
public class AuthenticationException extends BusinessException {

    public AuthenticationException(String message) {
        super("AUTHENTICATION_ERROR", message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super("AUTHENTICATION_ERROR", message, cause);
    }

    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException("Invalid email or password");
    }

    public static AuthenticationException accountLocked() {
        return new AuthenticationException("Account is temporarily locked due to too many failed attempts");
    }

    public static AuthenticationException accountDisabled() {
        return new AuthenticationException("Account is disabled");
    }

    public static AuthenticationException emailNotVerified() {
        return new AuthenticationException("Email address is not verified");
    }

    public static AuthenticationException tokenExpired() {
        return new AuthenticationException("Authentication token has expired");
    }

    public static AuthenticationException invalidToken() {
        return new AuthenticationException("Invalid authentication token");
    }

    public static AuthenticationException tokenRequired() {
        return new AuthenticationException("Authentication token is required");
    }

    public static AuthenticationException refreshTokenExpired() {
        return new AuthenticationException("Refresh token has expired");
    }

    public static AuthenticationException invalidRefreshToken() {
        return new AuthenticationException("Invalid refresh token");
    }

    public static AuthenticationException sessionExpired() {
        return new AuthenticationException("User session has expired");
    }

    public static AuthenticationException tooManyAttempts() {
        return new AuthenticationException("Too many login attempts. Please try again later");
    }

    public static AuthenticationException passwordResetRequired() {
        return new AuthenticationException("Password reset is required");
    }
}