package com.interviai.backend.module.resume.exception;

/**
 * Exception thrown when resume processing fails.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
public class ResumeProcessingException extends RuntimeException {

    public ResumeProcessingException(String message) {
        super(message);
    }

    public ResumeProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public static ResumeProcessingException textExtractionFailed(Throwable cause) {
        return new ResumeProcessingException("Failed to extract text from resume", cause);
    }

    public static ResumeProcessingException parsingFailed(Throwable cause) {
        return new ResumeProcessingException("Failed to parse resume content", cause);
    }

    public static ResumeProcessingException fileSaveFailed(Throwable cause) {
        return new ResumeProcessingException("Failed to save resume file", cause);
    }

    public static ResumeProcessingException processingTimeout() {
        return new ResumeProcessingException("Resume processing timed out");
    }

    public static ResumeProcessingException processingLimitExceeded() {
        return new ResumeProcessingException("Resume processing limit exceeded");
    }

    public static ResumeProcessingException invalidProcessingState(String currentState) {
        return new ResumeProcessingException("Invalid processing state: " + currentState);
    }
}