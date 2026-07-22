package com.interviai.backend.common.exception;

/**
 * Exception thrown when external service calls fail.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
public class ExternalServiceException extends BusinessException {

    private final String serviceName;

    public ExternalServiceException(String serviceName, String message) {
        super("EXTERNAL_SERVICE_ERROR", String.format("%s service error: %s", serviceName, message));
        this.serviceName = serviceName;
    }

    public ExternalServiceException(String serviceName, String message, Throwable cause) {
        super("EXTERNAL_SERVICE_ERROR", String.format("%s service error: %s", serviceName, message), cause);
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public static ExternalServiceException aiService(String message) {
        return new ExternalServiceException("AI", message);
    }

    public static ExternalServiceException aiService(String message, Throwable cause) {
        return new ExternalServiceException("AI", message, cause);
    }

    public static ExternalServiceException emailService(String message) {
        return new ExternalServiceException("Email", message);
    }

    public static ExternalServiceException emailService(String message, Throwable cause) {
        return new ExternalServiceException("Email", message, cause);
    }

    public static ExternalServiceException storageService(String message) {
        return new ExternalServiceException("Storage", message);
    }

    public static ExternalServiceException storageService(String message, Throwable cause) {
        return new ExternalServiceException("Storage", message, cause);
    }

    public static ExternalServiceException paymentService(String message) {
        return new ExternalServiceException("Payment", message);
    }

    public static ExternalServiceException paymentService(String message, Throwable cause) {
        return new ExternalServiceException("Payment", message, cause);
    }
}