package com.interviai.backend.module.evaluation.dto;

import lombok.Data;

import java.util.UUID;
import java.util.List;
import java.util.Map;

@Data
public class SkillGapAnalysisResponse {
    private UUID userId;
    private String targetRole;
    private Double overallReadiness;
    private String readinessLevel; // READY, ALMOST_READY, NEEDS_PREPARATION, SIGNIFICANT_GAP
    
    // Skills matching
    private SkillsMatching skillsMatching;
    
    // Learning recommendations
    private LearningRecommendations learningRecommendations;
    
    // Roadmap
    private List<LearningPhase> learningRoadmap;
    
    // Interview performance trends
    private PerformanceTrends performanceTrends;
    
    @Data
    public static class SkillsMatching {
        private List<String> matchedSkills;
        private List<SkillGap> skillGaps;
        private Double matchPercentage;
        private Map<String, Double> categoryMatchPercentages;
    }
    
    @Data
    public static class SkillGap {
        private String skillName;
        private String currentLevel;
        private String requiredLevel;
        private String importance; // CRITICAL, HIGH, MEDIUM, LOW
        private String gapSize; // LARGE, MEDIUM, SMALL
    }
    
    @Data
    public static class LearningRecommendations {
        private List<LearningResource> immediateActions;
        private List<LearningResource> shortTermGoals;
        private List<LearningResource> longTermGoals;
        private Integer estimatedWeeksToCloseGap;
    }
    
    @Data
    public static class LearningResource {
        private String title;
        private String description;
        private String resourceType; // COURSE, BOOK, PROJECT, PRACTICE, CERTIFICATION
        private String provider;
        private String url;
        private Integer estimatedHours;
        private List<String> skillsCovered;
    }
    
    @Data
    public static class LearningPhase {
        private Integer phaseNumber;
        private String phaseName;
        private String description;
        private Integer durationWeeks;
        private List<String> skillsToAcquire;
        private List<String> milestones;
        private List<LearningResource> resources;
    }
    
    @Data
    public static class PerformanceTrends {
        private Double averageScore;
        private Double improvementRate;
        private String trend; // IMPROVING, STABLE, DECLINING
        private Map<String, Double> categoryTrends;
        private List<InterviewScoreTrend> recentInterviews;
    }
    
    @Data
    public static class InterviewScoreTrend {
        private String date;
        private Double score;
        private String role;
        private String company;
    }
}
