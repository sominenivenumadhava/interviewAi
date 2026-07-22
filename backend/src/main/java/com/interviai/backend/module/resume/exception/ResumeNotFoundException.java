package com.interviai.backend.module.resume.exception;

import java.util.UUID;

/**
 * Exception thrown when resume is not found.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
public class ResumeNotFoundException extends RuntimeException {

    public ResumeNotFoundException(String message) {
        super(message);
    }

    public ResumeNotFoundException(UUID resumeId) {
        super("Resume not found with ID: " + resumeId);
    }

    public ResumeNotFoundException(UUID resumeId, UUID userId) {
        super("Resume not found with ID: " + resumeId + " for user: " + userId);
    }

    public ResumeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}