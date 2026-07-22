package com.interviai.backend.module.interview.dto.response;

import lombok.Data;

import java.util.UUID;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class InterviewAnswerResponse {
    private UUID id;
    private String answerText;
    private String answerAudioUrl;
    private String answerVideoUrl;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
    private Long timeTakenSeconds;
    private Double score;
    private String rating;
    private List<String> strengths;
    private List<String> improvements;
    private String feedback;
    private String suggestedAnswer;
    private Double confidenceScore;
    private Double clarityScore;
    private Double relevanceScore;
    private Double technicalAccuracyScore;
}
