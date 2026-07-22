package com.interviai.backend.common.exception;

import lombok.Getter;

/**
 * Base exception for business logic violations.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final Object[] arguments;

    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
        this.arguments = new Object[0];
    }

    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.arguments = new Object[0];
    }

    public BusinessException(String errorCode, String message, Object... arguments) {
        super(message);
        this.errorCode = errorCode;
        this.arguments = arguments;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BUSINESS_ERROR";
        this.arguments = new Object[0];
    }

    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.arguments = new Object[0];
    }

    public BusinessException(String errorCode, String message, Throwable cause, Object... arguments) {
        super(message, cause);
        this.errorCode = errorCode;
        this.arguments = arguments;
    }
}