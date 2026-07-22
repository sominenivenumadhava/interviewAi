package com.interviai.backend.module.resume.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for resume update.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeUpdateRequest {

    @Size(min = 3, max = 200, message = "Resume title must be between 3 and 200 characters")
    private String title;

    @Size(max = 1000, message = "Summary cannot exceed 1000 characters")
    private String summary;

    @Size(max = 200, message = "Full name cannot exceed 200 characters")
    private String fullName;

    @Size(max = 255, message = "Email cannot exceed 255 characters")
    private String email;

    @Size(max = 20, message = "Phone cannot exceed 20 characters")
    private String phone;

    @Size(max = 200, message = "Location cannot exceed 200 characters")
    private String location;

    private Integer totalExperienceYears;

    private Integer totalExperienceMonths;

    private Boolean isPrimary;
}