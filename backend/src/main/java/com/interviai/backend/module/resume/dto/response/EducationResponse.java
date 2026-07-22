package com.interviai.backend.module.resume.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Response DTO for Education entity.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EducationResponse {

    private UUID id;
    
    private String institutionName;
    
    private String degree;
    
    private String fieldOfStudy;
    
    private String degreeType;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    private Boolean isCurrent;
    
    private Double gpa;
    
    private Double maxGpa;
    
    private String formattedGpa;
    
    private Double percentage;
    
    private String grade;
    
    private String location;
    
    private String description;
    
    private String honors;
    
    private String relevantCoursework;
    
    private String activities;
    
    private Integer displayOrder;
    
    // Computed fields
    private String formattedDateRange;
    
    private String fullDegreeName;
    
    private Double durationInYears;
    
    private Boolean hasPerformanceMetrics;
}