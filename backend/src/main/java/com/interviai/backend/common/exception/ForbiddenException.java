package com.interviai.backend.common.exception;

/**
 * Exception thrown when user is authenticated but lacks required permissions.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
public class ForbiddenException extends BusinessException {

    public ForbiddenException() {
        super("FORBIDDEN", "Access denied");
    }

    public ForbiddenException(String message) {
        super("FORBIDDEN", message);
    }

    public static ForbiddenException insufficientPermissions() {
        return new ForbiddenException("Insufficient permissions to access this resource");
    }

    public static ForbiddenException resourceAccess(String resourceType) {
        return new ForbiddenException(String.format("Access denied to %s", resourceType));
    }

    public static ForbiddenException operationNotAllowed(String operation) {
        return new ForbiddenException(String.format("Operation '%s' is not allowed", operation));
    }

    public static ForbiddenException adminRequired() {
        return new ForbiddenException("Administrative privileges required");
    }

    public static ForbiddenException ownershipRequired() {
        return new ForbiddenException("Resource ownership required");
    }
}