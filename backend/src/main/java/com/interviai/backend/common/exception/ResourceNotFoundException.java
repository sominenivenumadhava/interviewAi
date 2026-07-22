package com.interviai.backend.common.exception;

import java.util.UUID;

/**
 * Exception thrown when a requested resource is not found.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resourceName, UUID id) {
        super("RESOURCE_NOT_FOUND", String.format("%s not found with id: %s", resourceName, id));
    }

    public ResourceNotFoundException(String resourceName, String field, Object value) {
        super("RESOURCE_NOT_FOUND", String.format("%s not found with %s: %s", resourceName, field, value));
    }

    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message);
    }

    public static ResourceNotFoundException user(UUID userId) {
        return new ResourceNotFoundException("User", userId);
    }

    public static ResourceNotFoundException userByEmail(String email) {
        return new ResourceNotFoundException("User", "email", email);
    }

    public static ResourceNotFoundException interview(UUID interviewId) {
        return new ResourceNotFoundException("Interview", interviewId);
    }

    public static ResourceNotFoundException question(UUID questionId) {
        return new ResourceNotFoundException("Question", questionId);
    }

    public static ResourceNotFoundException answer(UUID answerId) {
        return new ResourceNotFoundException("Answer", answerId);
    }

    public static ResourceNotFoundException resume(UUID resumeId) {
        return new ResourceNotFoundException("Resume", resumeId);
    }

    public static ResourceNotFoundException evaluation(UUID evaluationId) {
        return new ResourceNotFoundException("Evaluation", evaluationId);
    }

    public static ResourceNotFoundException company(UUID companyId) {
        return new ResourceNotFoundException("Company", companyId);
    }

    public static ResourceNotFoundException jobRole(UUID roleId) {
        return new ResourceNotFoundException("Job Role", roleId);
    }
}