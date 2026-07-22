package com.interviai.backend.module.analytics.service;

import java.util.UUID;
import com.interviai.backend.module.analytics.dto.UserAnalyticsResponse;
import java.util.UUID;
import com.interviai.backend.module.analytics.dto.CompanyAnalyticsResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AnalyticsService {
    
    /**
     * Get comprehensive analytics for a user
     * @param userId User ID
     * @param startDate Start date for analytics period
     * @param endDate End date for analytics period
     * @return User analytics data
     */
    UserAnalyticsResponse getUserAnalytics(UUID userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Get analytics for a specific company
     * @param userId User ID
     * @param company Company name
     * @return Company-specific analytics
     */
    CompanyAnalyticsResponse getCompanyAnalytics(UUID userId, String company);
    
    /**
     * Get performance comparison across different roles
     * @param userId User ID
     * @return Role comparison data
     */
    List<UserAnalyticsResponse.CategoryPerformance> getRoleComparison(UUID userId);
    
    /**
     * Get skill progress over time
     * @param userId User ID
     * @param skillName Specific skill name (optional)
     * @return Skill progress data
     */
    List<UserAnalyticsResponse.SkillProgress> getSkillProgress(UUID userId, String skillName);
    
    /**
     * Get interview preparation insights
     * @param userId User ID
     * @param targetRole Target role
     * @return Preparation insights and recommendations
     */
    PreparationInsights getPreparationInsights(UUID userId, String targetRole);
    
    /**
     * Export analytics data
     * @param userId User ID
     * @param format Export format (CSV, PDF, JSON)
     * @return Exported data as byte array
     */
    byte[] exportAnalytics(UUID userId, String format);
    
    @lombok.Data
    class PreparationInsights {
        private String targetRole;
        private Double readinessScore;
        private Integer estimatedInterviewsNeeded;
        private List<String> focusAreas;
        private List<String> strongAreas;
        private Map<String, Integer> recommendedPracticeHours;
        private List<String> suggestedResources;
    }
}
