package com.interviai.backend.module.resume.exception;

/**
 * Exception thrown when resume format is invalid.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
public class InvalidResumeFormatException extends RuntimeException {

    public InvalidResumeFormatException(String message) {
        super(message);
    }

    public InvalidResumeFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public static InvalidResumeFormatException unsupportedFileType(String contentType) {
        return new InvalidResumeFormatException("Unsupported file type: " + contentType);
    }

    public static InvalidResumeFormatException fileTooLarge(long fileSize, long maxSize) {
        return new InvalidResumeFormatException(
                String.format("File size %d bytes exceeds maximum allowed size of %d bytes", fileSize, maxSize));
    }

    public static InvalidResumeFormatException fileTooSmall(long fileSize, long minSize) {
        return new InvalidResumeFormatException(
                String.format("File size %d bytes is below minimum required size of %d bytes", fileSize, minSize));
    }

    public static InvalidResumeFormatException invalidFileName(String fileName) {
        return new InvalidResumeFormatException("Invalid file name: " + fileName);
    }

    public static InvalidResumeFormatException corruptedFile() {
        return new InvalidResumeFormatException("File appears to be corrupted or unreadable");
    }
}