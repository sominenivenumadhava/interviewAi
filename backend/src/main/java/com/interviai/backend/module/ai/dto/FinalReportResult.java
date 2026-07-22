package com.interviai.backend.module.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

/**
 * Enterprise-level final interview report result DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalReportResult {

    private Integer overallScore;
    private String hiringProbability; // e.g. "85%"
    private Integer companyReadinessScore; // 0-100

    private Map<String, Integer> categoryScores; // Communication, Technical, System Design, Behavioral, Confidence

    private List<String> keyStrengths;
    private List<String> keyWeaknesses;
    private List<MistakeSummary> criticalMistakes;

    private List<String> recommendedLearningPath;
    private List<String> recommendedLeetCode;
    private List<String> recommendedProjects;
    private List<String> recommendedCourses;

    private String executiveSummary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MistakeSummary {
        private String question;
        private String candidateAnswer;
        private String idealApproach;
        private String keyTakeaway;
    }
}
