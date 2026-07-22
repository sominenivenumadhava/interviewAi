package com.interviai.backend.module.analytics.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class UserAnalyticsResponse {
    private java.util.UUID userId;
    private OverviewMetrics overviewMetrics;
    private PerformanceMetrics performanceMetrics;
    private SkillMetrics skillMetrics;
    private TimeMetrics timeMetrics;
    private List<InterviewHistory> recentInterviews;
    private Map<String, ChartData> charts;
    
    @Data
    public static class OverviewMetrics {
        private Integer totalInterviews;
        private Integer completedInterviews;
        private Integer upcomingInterviews;
        private Double averageScore;
        private String overallRating;
        private Double improvementRate;
        private Integer currentStreak;
        private Integer longestStreak;
    }
    
    @Data
    public static class PerformanceMetrics {
        private Double currentMonthScore;
        private Double lastMonthScore;
        private Double monthOverMonthGrowth;
        private Map<String, Double> scoresByDifficulty;
        private Map<String, Double> scoresByInterviewType;
        private List<PerformanceTrend> monthlyTrends;
        private TopPerformance bestPerformance;
        private Map<String, CategoryPerformance> categoryPerformance;
    }
    
    @Data
    public static class SkillMetrics {
        private List<SkillScore> topSkills;
        private List<SkillScore> weakSkills;
        private List<SkillProgress> skillProgress;
        private Integer totalSkillsAssessed;
        private Map<String, Double> skillCategoryScores;
    }
    
    @Data
    public static class TimeMetrics {
        private Double averageInterviewDuration;
        private Double averageTimePerQuestion;
        private Integer totalPracticeHours;
        private Map<String, Integer> practiceHoursByMonth;
        private List<PracticeSession> recentSessions;
    }
    
    @Data
    public static class InterviewHistory {
        private String sessionId;
        private String role;
        private String company;
        private LocalDate date;
        private Double score;
        private String rating;
        private String difficulty;
        private String type;
        private Integer duration;
    }
    
    @Data
    public static class ChartData {
        private String chartType;
        private List<String> labels;
        private List<Dataset> datasets;
    }
    
    @Data
    public static class Dataset {
        private String label;
        private List<Double> data;
        private String backgroundColor;
        private String borderColor;
    }
    
    @Data
    public static class PerformanceTrend {
        private String month;
        private Double averageScore;
        private Integer interviewCount;
        private Double growthRate;
    }
    
    @Data
    public static class TopPerformance {
        private String role;
        private String company;
        private Double score;
        private LocalDate date;
    }
    
    @Data
    public static class CategoryPerformance {
        private String category;
        private Double averageScore;
        private Integer questionCount;
        private Double improvementRate;
        private String trend;
    }
    
    @Data
    public static class SkillScore {
        private String skillName;
        private Double score;
        private String proficiencyLevel;
        private Integer assessmentCount;
    }
    
    @Data
    public static class SkillProgress {
        private String skillName;
        private Double initialScore;
        private Double currentScore;
        private Double improvement;
        private String trend;
    }
    
    @Data
    public static class PracticeSession {
        private LocalDate date;
        private Integer durationMinutes;
        private String type;
        private Double score;
    }
}