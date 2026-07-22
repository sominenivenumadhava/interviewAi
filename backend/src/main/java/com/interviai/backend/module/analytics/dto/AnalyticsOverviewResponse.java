package com.interviai.backend.module.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsOverviewResponse {

    private OverallMetrics overallMetrics;
    private List<MonthlyTrend> scoreTrends;
    private List<WeeklyPractice> weeklyPractice;
    private Map<String, Integer> skillHeatmap;
    private Map<String, Integer> companyPerformance;
    private Map<String, Integer> rolePerformance;
    private Map<String, Integer> difficultyDistribution;
    private List<SkillDetail> strongSkills;
    private List<SkillDetail> weakSkills;
    private List<String> recommendedSkills;
    private List<ActivityItem> recentActivity;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverallMetrics {
        private Double averageScore;
        private Integer totalInterviews;
        private Double completionRate; // percentage
        private Double averageDurationMinutes;
        private Double totalPracticeHours;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyTrend {
        private String period; // e.g. "Week 1", "Jan 2026"
        private Double averageScore;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeeklyPractice {
        private String day; // Mon, Tue, etc.
        private Double hours;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillDetail {
        private String skillName;
        private Integer score;
        private String category;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityItem {
        private String title;
        private String description;
        private String timestamp;
        private String type;
    }
}
