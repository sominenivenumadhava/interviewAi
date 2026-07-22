package com.interviai.backend.module.dashboard.service;

import com.interviai.backend.module.dashboard.dto.DashboardResponse;

public interface DashboardService {
    
    /**
     * Get comprehensive dashboard data for a user
     * @param userId User ID
     * @return Dashboard data including stats, activities, and insights
     */
    DashboardResponse getDashboard(java.util.UUID userId);
    
    /**
     * Refresh dashboard data and cache it
     * @param userId User ID
     * @return Updated dashboard data
     */
    DashboardResponse refreshDashboard(java.util.UUID userId);
}