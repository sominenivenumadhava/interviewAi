package com.interviai.backend.module.resume.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Response DTO for WorkExperience entity.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkExperienceResponse {

    private UUID id;
    
    private String companyName;
    
    private String jobTitle;
    
    private String employmentType;
    
    private String location;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    private Boolean isCurrent;
    
    private String description;
    
    private String keyAchievements;
    
    private String technologiesUsed;
    
    private String industry;
    
    private String companySize;
    
    private String reportingTo;
    
    private Integer teamSize;
    
    private String salaryRange;
    
    private Integer displayOrder;
    
    // Computed fields
    private String formattedDateRange;
    
    private String formattedDuration;
    
    private Integer durationInMonths;
    
    private Double experienceInYears;
    
    private String fullPositionTitle;
    
    private Boolean hasDetailedInfo;
}