package com.interviai.backend.module.resume.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for Project entity.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {

    private UUID id;
    
    private String name;
    
    private String description;
    
    private String role;
    
    private String organization;
    
    private String projectType;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    private Boolean isOngoing;
    
    private String technologiesUsed;
    
    private String keyAchievements;
    
    private String challengesFaced;
    
    private String projectUrl;
    
    private String githubUrl;
    
    private String demoUrl;
    
    private Integer teamSize;
    
    private String budgetRange;
    
    private String clientName;
    
    private String industry;
    
    private Integer displayOrder;
    
    // Computed fields
    private String formattedDateRange;
    
    private String formattedDuration;
    
    private Integer durationInMonths;
    
    private Boolean hasExternalLinks;
    
    private List<String> externalLinks;
    
    private Boolean hasDetailedInfo;
    
    private Integer complexityScore;
    
    private List<String> technologiesList;
}