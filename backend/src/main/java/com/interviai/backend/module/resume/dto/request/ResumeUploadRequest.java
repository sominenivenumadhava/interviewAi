package com.interviai.backend.module.resume.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for resume upload.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeUploadRequest {

    @NotBlank(message = "Resume title is required")
    @Size(min = 3, max = 200, message = "Resume title must be between 3 and 200 characters")
    private String title;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Builder.Default
    private Boolean setPrimary = false;

    @Builder.Default
    private Boolean replaceExisting = false;
}