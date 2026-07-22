package com.interviai.backend.module.resume.validator;

import com.interviai.backend.module.resume.dto.request.ResumeUploadRequest;
import com.interviai.backend.module.resume.dto.request.ResumeUpdateRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Validator for Resume operations.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Component
public class ResumeValidator {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private static final Set<String> ALLOWED_FILE_EXTENSIONS = Set.of(
            ".pdf", ".doc", ".docx"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final long MIN_FILE_SIZE = 1024; // 1KB

    /**
     * Validate resume upload request.
     */
    public List<String> validateResumeUpload(ResumeUploadRequest request, MultipartFile file) {
        List<String> errors = new ArrayList<>();

        if (request == null) {
            errors.add("Request cannot be null");
            return errors;
        }

        // Validate file
        errors.addAll(validateFile(file));

        // Validate request fields
        errors.addAll(validateUploadRequest(request));

        return errors;
    }

    /**
     * Validate resume update request.
     */
    public List<String> validateResumeUpdate(ResumeUpdateRequest request) {
        List<String> errors = new ArrayList<>();

        if (request == null) {
            errors.add("Request cannot be null");
            return errors;
        }

        // Validate title if provided
        if (request.getTitle() != null) {
            if (request.getTitle().trim().isEmpty()) {
                errors.add("Title cannot be empty");
            } else if (request.getTitle().length() < 3) {
                errors.add("Title must be at least 3 characters long");
            } else if (request.getTitle().length() > 200) {
                errors.add("Title cannot exceed 200 characters");
            }
        }

        // Validate summary if provided
        if (request.getSummary() != null && request.getSummary().length() > 1000) {
            errors.add("Summary cannot exceed 1000 characters");
        }

        // Validate full name if provided
        if (request.getFullName() != null) {
            if (request.getFullName().trim().isEmpty()) {
                errors.add("Full name cannot be empty");
            } else if (request.getFullName().length() > 200) {
                errors.add("Full name cannot exceed 200 characters");
            } else if (!isValidName(request.getFullName())) {
                errors.add("Full name contains invalid characters");
            }
        }

        // Validate email if provided
        if (request.getEmail() != null) {
            if (!request.getEmail().trim().isEmpty() && !isValidEmail(request.getEmail())) {
                errors.add("Invalid email format");
            } else if (request.getEmail().length() > 255) {
                errors.add("Email cannot exceed 255 characters");
            }
        }

        // Validate phone if provided
        if (request.getPhone() != null) {
            if (!request.getPhone().trim().isEmpty() && !isValidPhone(request.getPhone())) {
                errors.add("Invalid phone number format");
            } else if (request.getPhone().length() > 20) {
                errors.add("Phone number cannot exceed 20 characters");
            }
        }

        // Validate location if provided
        if (request.getLocation() != null && request.getLocation().length() > 200) {
            errors.add("Location cannot exceed 200 characters");
        }

        // Validate experience if provided
        if (request.getTotalExperienceYears() != null) {
            if (request.getTotalExperienceYears() < 0) {
                errors.add("Experience years cannot be negative");
            } else if (request.getTotalExperienceYears() > 50) {
                errors.add("Experience years cannot exceed 50");
            }
        }

        if (request.getTotalExperienceMonths() != null) {
            if (request.getTotalExperienceMonths() < 0) {
                errors.add("Experience months cannot be negative");
            } else if (request.getTotalExperienceMonths() > 11) {
                errors.add("Experience months cannot exceed 11");
            }
        }

        return errors;
    }

    /**
     * Validate uploaded file.
     */
    public List<String> validateFile(MultipartFile file) {
        List<String> errors = new ArrayList<>();

        if (file == null) {
            errors.add("File is required");
            return errors;
        }

        if (file.isEmpty()) {
            errors.add("File cannot be empty");
            return errors;
        }

        // Validate file size
        long fileSize = file.getSize();
        if (fileSize < MIN_FILE_SIZE) {
            errors.add("File is too small. Minimum size is 1KB");
        } else if (fileSize > MAX_FILE_SIZE) {
            errors.add("File is too large. Maximum size is 10MB");
        }

        // Validate file name
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            errors.add("File name is required");
        } else {
            // Check file extension
            String extension = getFileExtension(originalFilename).toLowerCase();
            if (!ALLOWED_FILE_EXTENSIONS.contains(extension)) {
                errors.add("File type not supported. Allowed types: PDF, DOC, DOCX");
            }

            // Validate filename length and characters
            if (originalFilename.length() > 255) {
                errors.add("File name is too long. Maximum 255 characters");
            }

            if (!isValidFilename(originalFilename)) {
                errors.add("File name contains invalid characters");
            }
        }

        // Validate content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            errors.add("Invalid file type. Only PDF, DOC, and DOCX files are allowed");
        }

        return errors;
    }

    /**
     * Validate upload request fields.
     */
    private List<String> validateUploadRequest(ResumeUploadRequest request) {
        List<String> errors = new ArrayList<>();

        // Title is required and validated by annotation, but add custom validation
        if (request.getTitle() != null) {
            String trimmedTitle = request.getTitle().trim();
            if (containsInvalidCharacters(trimmedTitle)) {
                errors.add("Title contains invalid characters");
            }
        }

        return errors;
    }

    /**
     * Check if email format is valid.
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex) && email.length() <= 255;
    }

    /**
     * Check if phone number format is valid.
     */
    private boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        
        // Allow digits, spaces, parentheses, hyphens, and plus sign
        String phoneRegex = "^[+]?[\\d\\s\\-\\(\\)]+$";
        String cleanPhone = phone.replaceAll("[\\s\\-\\(\\)]", "");
        
        return phone.matches(phoneRegex) && 
               cleanPhone.length() >= 7 && 
               cleanPhone.length() <= 15;
    }

    /**
     * Check if name format is valid.
     */
    private boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        // Allow letters, spaces, apostrophes, hyphens, and dots
        String nameRegex = "^[a-zA-Z\\s'\\-\\.]+$";
        return name.matches(nameRegex);
    }

    /**
     * Check if filename is valid.
     */
    private boolean isValidFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }
        
        // Disallow certain characters that might cause issues
        String[] invalidChars = {"<", ">", ":", "\"", "|", "?", "*", "\0"};
        for (String invalidChar : invalidChars) {
            if (filename.contains(invalidChar)) {
                return false;
            }
        }
        
        // Disallow certain reserved names on Windows
        String[] reservedNames = {"CON", "PRN", "AUX", "NUL", 
                                  "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
                                  "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};
        
        String nameWithoutExtension = filename.substring(0, filename.lastIndexOf('.') != -1 ? filename.lastIndexOf('.') : filename.length());
        for (String reserved : reservedNames) {
            if (nameWithoutExtension.equalsIgnoreCase(reserved)) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Check if text contains invalid characters.
     */
    private boolean containsInvalidCharacters(String text) {
        if (text == null) {
            return false;
        }
        
        // Check for control characters (except newline, carriage return, and tab)
        for (char c : text.toCharArray()) {
            if (Character.isISOControl(c) && c != '\n' && c != '\r' && c != '\t') {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Get file extension from filename.
     */
    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        
        return filename.substring(lastDotIndex);
    }

    /**
     * Check if file type is supported.
     */
    public boolean isFileTypeSupported(String contentType) {
        return contentType != null && ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase());
    }

    /**
     * Check if file extension is supported.
     */
    public boolean isFileExtensionSupported(String filename) {
        if (filename == null) {
            return false;
        }
        
        String extension = getFileExtension(filename).toLowerCase();
        return ALLOWED_FILE_EXTENSIONS.contains(extension);
    }

    /**
     * Get maximum allowed file size.
     */
    public long getMaxFileSize() {
        return MAX_FILE_SIZE;
    }

    /**
     * Get minimum allowed file size.
     */
    public long getMinFileSize() {
        return MIN_FILE_SIZE;
    }

    /**
     * Get allowed content types.
     */
    public Set<String> getAllowedContentTypes() {
        return ALLOWED_CONTENT_TYPES;
    }

    /**
     * Get allowed file extensions.
     */
    public Set<String> getAllowedFileExtensions() {
        return ALLOWED_FILE_EXTENSIONS;
    }
}