package com.interviai.backend.module.analytics.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class CompanyAnalyticsResponse {
    private String company;
    private CompanyOverview overview;
    private DifficultyDistribution difficultyDistribution;
    private List<RoleStatistics> roleStatistics;
    private List<CommonQuestion> commonQuestions;
    private List<SuccessStory> successStories;
    private Map<String, Double> skillRequirements;
    
    @Data
    public static class CompanyOverview {
        private Integer totalInterviews;
        private Double averageScore;
        private Double successRate;
        private String mostCommonRole;
        private String preferredInterviewType;
        private Integer averageDuration;
    }
    
    @Data
    public static class DifficultyDistribution {
        private Integer easyCount;
        private Integer mediumCount;
        private Integer hardCount;
        private Integer expertCount;
        private Double averageDifficulty;
    }
    
    @Data
    public static class RoleStatistics {
        private String role;
        private Integer interviewCount;
        private Double averageScore;
        private Double successRate;
        private List<String> topSkillsRequired;
    }
    
    @Data
    public static class CommonQuestion {
        private String question;
        private String category;
        private Integer frequency;
        private Double averageScore;
        private String difficulty;
    }
    
    @Data
    public static class SuccessStory {
        private String role;
        private Double score;
        private LocalDate date;
        private List<String> keyStrengths;
    }
}