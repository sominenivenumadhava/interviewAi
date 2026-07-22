package com.interviai.backend.module.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class AnswerValidationRequest {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @NotBlank(message = "Question is required")
        private String question;

        @NotBlank(message = "Transcript is required")
        private String transcript;

        private String role;
        private String company;
        private String interviewType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private boolean isRelevant;
        private Double score; // out of 10
        private Integer confidence; // percentage e.g. 95
        private String feedback;
        private List<String> missingPoints;
        private List<String> strengths;
        private List<String> weaknesses;
        private String idealAnswer;
    }
}
