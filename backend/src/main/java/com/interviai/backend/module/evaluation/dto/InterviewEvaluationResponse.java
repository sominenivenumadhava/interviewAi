package com.interviai.backend.module.evaluation.dto;

import com.interviai.backend.module.interview.enums.DifficultyLevel;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class InterviewEvaluationResponse {
    private UUID interviewId;
    private String sessionId;
    private String role;
    private String company;
    private String interviewType;
    private DifficultyLevel difficultyLevel;
    private LocalDateTime completedAt;

    private Integer totalQuestions;
    private Integer answeredQuestions;

    // Overall scores (0–100)
    private Double overallScore;
    private String overallRating;
    private String performanceLevel;
    private Double hiringProbability;

    // Dimension scores (0–100)
    private CategoryScores categoryScores;

    private List<String> topStrengths;
    private List<String> keyImprovements;
    private List<String> recommendedPractice;

    private String detailedFeedback;
    private String recommendations;

    private List<QuestionPerformance> questionPerformances;
    private SkillsAssessment skillsAssessment;
    private BenchmarkComparison benchmarkComparison;

    @Data
    public static class CategoryScores {
        private Double technicalScore;
        private Double communicationScore;
        private Double problemSolvingScore;
        private Double behavioralScore;
        private Double domainKnowledgeScore;
        private Double correctnessScore;
        private Double optimizationScore;
        private Double timeManagementScore;
        private Double confidenceScore;
    }

    @Data
    public static class QuestionPerformance {
        private Integer questionOrder;
        private String questionText;
        private String category;
        private String userAnswer;
        private String feedback;
        private Double score;
        private String rating;
        private Long timeTakenSeconds;
        private Boolean exceedsExpectedTime;
        private List<String> strengths;
        private List<String> weaknesses;
    }

    @Data
    public static class SkillsAssessment {
        private List<SkillRating> technicalSkills;
        private List<SkillRating> softSkills;
        private List<String> demonstratedSkills;
        private List<String> skillGaps;
    }

    @Data
    public static class SkillRating {
        private String skillName;
        private String proficiencyLevel;
        private Double score;
    }

    @Data
    public static class BenchmarkComparison {
        private Double averageScoreForRole;
        private Double percentileRank;
        private String comparisonMessage;
        private Map<String, Double> categoryComparisons;
    }
}
