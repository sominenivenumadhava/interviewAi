package com.interviai.backend.module.interview.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class InterviewSessionResponse {
    private InterviewResponse interview;
    private List<InterviewQuestionResponse> questions;
    private InterviewProgressResponse progress;
    
    @Data
    public static class InterviewProgressResponse {
        private Integer totalQuestions;
        private Integer answeredQuestions;
        private Integer currentQuestionOrder;
        private Double completionPercentage;
        private Long elapsedTimeSeconds;
        private Long remainingTimeSeconds;
    }
}