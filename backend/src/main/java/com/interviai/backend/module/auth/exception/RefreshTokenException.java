package com.interviai.backend.module.auth.exception;

import com.interviai.backend.common.exception.BusinessException;

/**
 * Exception for refresh token related errors.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
public class RefreshTokenException extends BusinessException {

    public RefreshTokenException(String message) {
        super("REFRESH_TOKEN_ERROR", message);
    }

    public RefreshTokenException(String message, Throwable cause) {
        super("REFRESH_TOKEN_ERROR", message, cause);
    }

    public static RefreshTokenException expired() {
        return new RefreshTokenException("Refresh token has expired");
    }

    public static RefreshTokenException invalid() {
        return new RefreshTokenException("Invalid refresh token");
    }

    public static RefreshTokenException notFound() {
        return new RefreshTokenException("Refresh token not found");
    }

    public static RefreshTokenException revoked() {
        return new RefreshTokenException("Refresh token has been revoked");
    }

    public static RefreshTokenException creationFailed() {
        return new RefreshTokenException("Failed to create refresh token");
    }

    public static RefreshTokenException rotationFailed() {
        return new RefreshTokenException("Failed to rotate refresh token");
    }

    public static RefreshTokenException deviceMismatch() {
        return new RefreshTokenException("Token device mismatch");
    }

    public static RefreshTokenException limitExceeded() {
        return new RefreshTokenException("Maximum number of active tokens exceeded");
    }
}