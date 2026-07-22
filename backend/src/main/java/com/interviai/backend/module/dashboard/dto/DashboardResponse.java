package com.interviai.backend.module.dashboard.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DashboardResponse {
    private UserSummary userSummary;
    private InterviewStats interviewStats;
    private PerformanceOverview performanceOverview;
    private List<RecentActivity> recentActivities;
    private List<UpcomingInterview> upcomingInterviews;
    private SkillSnapshot skillSnapshot;
    private List<ActionItem> actionItems;
    private QuickInsights quickInsights;
    
    @Data
    public static class UserSummary {
        private java.util.UUID userId;
        private String fullName;
        private String email;
        private String currentLevel;
        private Integer totalPracticeHours;
        private LocalDateTime memberSince;
        private Boolean hasActiveSubscription;
        private String subscriptionPlan;
    }
    
    @Data
    public static class InterviewStats {
        private Integer totalInterviews;
        private Integer completedThisMonth;
        private Integer upcomingThisWeek;
        private Double successRate;
        private Integer currentStreak;
        private String mostPracticedRole;
        private String preferredDifficulty;
    }
    
    @Data
    public static class PerformanceOverview {
        private Double currentScore;
        private Double previousScore;
        private Double improvement;
        private String trend;
        private List<ScoreTrend> last30DaysTrend;
        private Double bestScore;
        private String bestScoreRole;
        private Double averageScore;
    }
    
    @Data
    public static class RecentActivity {
        private String type; // INTERVIEW_COMPLETED, SKILL_IMPROVED, MILESTONE_REACHED
        private String title;
        private String description;
        private LocalDateTime timestamp;
        private String icon;
        private String link;
    }
    
    @Data
    public static class UpcomingInterview {
        private java.util.UUID id;
        private String sessionId;
        private String role;
        private String company;
        private LocalDateTime scheduledAt;
        private String difficulty;
        private String type;
        private Integer durationMinutes;
        private String status;
    }
    
    @Data
    public static class SkillSnapshot {
        private List<TopSkill> topSkills;
        private List<ImprovingSkill> improvingSkills;
        private List<WeakSkill> weakSkills;
        private Integer totalSkillsAssessed;
        private LocalDateTime lastAssessedAt;
    }
    
    @Data
    public static class ActionItem {
        private String title;
        private String description;
        private String priority; // HIGH, MEDIUM, LOW
        private String category; // PRACTICE, LEARNING, REVIEW
        private String actionUrl;
        private Integer estimatedMinutes;
        private String icon;
    }
    
    @Data
    public static class QuickInsights {
        private String dailyTip;
        private String weeklyGoal;
        private Integer weeklyGoalProgress;
        private String motivationalQuote;
        private List<String> recommendedTopics;
        private String nextMilestone;
        private Integer pointsToNextLevel;
    }
    
    @Data
    public static class ScoreTrend {
        private String date;
        private Double score;
        private String label;
    }
    
    @Data
    public static class TopSkill {
        private String name;
        private Double score;
        private String level;
        private Integer rank;
    }
    
    @Data
    public static class ImprovingSkill {
        private String name;
        private Double improvementRate;
        private String trend;
    }
    
    @Data
    public static class WeakSkill {
        private String name;
        private Double score;
        private String recommendedAction;
    }
}