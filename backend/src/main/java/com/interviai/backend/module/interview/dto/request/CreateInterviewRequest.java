package com.interviai.backend.module.interview.dto.request;

import com.interviai.backend.module.interview.enums.DifficultyLevel;
import com.interviai.backend.module.interview.enums.InterviewType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateInterviewRequest {
    
    @NotBlank(message = "Role is required")
    @Size(max = 100, message = "Role must be less than 100 characters")
    private String role;
    
    @Size(max = 100, message = "Company name must be less than 100 characters")
    private String company;
    
    @Size(max = 5000, message = "Job description must be less than 5000 characters")
    private String jobDescription;
    
    @NotNull(message = "Interview type is required")
    private InterviewType interviewType;
    
    @NotNull(message = "Difficulty level is required")
    private DifficultyLevel difficultyLevel;
    
    @Min(value = 15, message = "Duration must be at least 15 minutes")
    @Max(value = 180, message = "Duration must not exceed 180 minutes")
    private Integer durationMinutes = 60;
    
    private java.util.UUID resumeId;
    
    private LocalDateTime scheduledAt;
    
    @Min(value = 1, message = "Number of questions must be at least 1")
    @Max(value = 50, message = "Number of questions must not exceed 50")
    private Integer numberOfQuestions = 10;
    
    private Boolean includeFollowUpQuestions = true;
    
    private Boolean includeCodingQuestions = false;
    
    private String focusAreas;
    
    private String customInstructions;
}