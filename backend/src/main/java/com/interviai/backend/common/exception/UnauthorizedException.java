package com.interviai.backend.common.exception;

/**
 * Exception thrown when user is not authenticated.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
public class UnauthorizedException extends BusinessException {

    public UnauthorizedException() {
        super("UNAUTHORIZED", "Authentication required");
    }

    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message);
    }

    public static UnauthorizedException invalidToken() {
        return new UnauthorizedException("Invalid or expired token");
    }

    public static UnauthorizedException tokenRequired() {
        return new UnauthorizedException("Authentication token required");
    }

    public static UnauthorizedException sessionExpired() {
        return new UnauthorizedException("Session has expired");
    }

    public static UnauthorizedException invalidCredentials() {
        return new UnauthorizedException("Invalid email or password");
    }

    public static UnauthorizedException accountDisabled() {
        return new UnauthorizedException("Account is disabled");
    }

    public static UnauthorizedException emailNotVerified() {
        return new UnauthorizedException("Email verification required");
    }
}