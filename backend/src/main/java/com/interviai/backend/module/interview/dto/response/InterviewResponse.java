package com.interviai.backend.module.interview.dto.response;

import com.interviai.backend.module.interview.enums.DifficultyLevel;
import com.interviai.backend.module.interview.enums.InterviewStatus;
import com.interviai.backend.module.interview.enums.InterviewType;
import lombok.Data;

import java.util.UUID;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class InterviewResponse {
    private UUID id;
    private String sessionId;
    private String title;
    private String company;
    private String role;
    private String jobDescription;
    private InterviewType interviewType;
    private DifficultyLevel difficultyLevel;
    private InterviewStatus status;
    private LocalDateTime scheduledAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer durationMinutes;
    private Long actualDurationSeconds;
    private Double overallScore;
    private String overallFeedback;
    private List<String> strengths;
    private List<String> improvements;
    private Integer totalQuestions;
    private Integer answeredQuestions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
