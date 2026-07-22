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
    private DifficultyLevel difficultyLevel;
    private LocalDateTime completedAt;
    
    // Overall scores
    private Double overallScore;
    private String overallRating;
    private String performanceLevel; // EXCELLENT, GOOD, AVERAGE, BELOW_AVERAGE, POOR
    
    // Category-wise scores
    private CategoryScores categoryScores;
    
    // Strengths and improvements
    private List<String> topStrengths;
    private List<String> keyImprovements;
    
    // Detailed feedback
    private String detailedFeedback;
    private String recommendations;
    
    // Question-wise performance
    private List<QuestionPerformance> questionPerformances;
    
    // Skills assessment
    private SkillsAssessment skillsAssessment;
    
    // Comparison with benchmarks
    private BenchmarkComparison benchmarkComparison;
    
    @Data
    public static class CategoryScores {
        private Double technicalScore;
        private Double communicationScore;
        private Double problemSolvingScore;
        private Double behavioralScore;
        private Double domainKnowledgeScore;
    }
    
    @Data
    public static class QuestionPerformance {
        private Integer questionOrder;
        private String questionText;
        private String category;
        private Double score;
        private String rating;
        private Long timeTakenSeconds;
        private Boolean exceedsExpectedTime;
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
        private String proficiencyLevel; // EXPERT, PROFICIENT, COMPETENT, DEVELOPING, NOVICE
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