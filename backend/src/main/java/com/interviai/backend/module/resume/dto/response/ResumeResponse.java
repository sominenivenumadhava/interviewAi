package com.interviai.backend.module.resume.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for Resume entity.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeResponse {

    private UUID id;
    
    private String title;
    
    private String originalFilename;
    
    private Long fileSize;
    
    private String formattedFileSize;
    
    private String contentType;
    
    private String fullName;
    
    private String email;
    
    private String phone;
    
    private String location;
    
    private String summary;
    
    private Integer totalExperienceYears;
    
    private Integer totalExperienceMonths;
    
    private String formattedExperience;
    
    private String status;
    
    private Boolean isPrimary;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime processingStartedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime processingCompletedAt;
    
    private String processingError;
    
    private Double parsingConfidenceScore;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // Related entities
    private List<EducationResponse> educations;
    
    private List<WorkExperienceResponse> workExperiences;
    
    private List<SkillResponse> skills;
    
    private List<ProjectResponse> projects;
    
    // Computed fields
    private Boolean isProcessingCompleted;
    
    private Boolean isProcessingFailed;
    
    private Boolean isProcessing;
    
    private String processingDuration;
    
    private Integer profileCompleteness;
}