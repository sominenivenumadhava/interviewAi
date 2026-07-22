package com.interviai.backend.module.dashboard.controller;

import com.interviai.backend.common.dto.ApiResponse;
import com.interviai.backend.module.dashboard.dto.DashboardResponse;
import com.interviai.backend.module.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "User dashboard APIs")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {
    
    @Autowired
    private DashboardService dashboardService;
    
    @GetMapping
    @Operation(summary = "Get user dashboard data")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(Authentication authentication) {
        java.util.UUID userId = getUserId(authentication);
        DashboardResponse response = dashboardService.getDashboard(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "Refresh dashboard data (clears cache)")
    public ResponseEntity<ApiResponse<DashboardResponse>> refreshDashboard(Authentication authentication) {
        java.util.UUID userId = getUserId(authentication);
        DashboardResponse response = dashboardService.refreshDashboard(userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Dashboard refreshed successfully"));
    }
    
    private java.util.UUID getUserId(Authentication authentication) {
        if (authentication.getPrincipal() instanceof com.interviai.backend.module.user.entity.User) {
            return ((com.interviai.backend.module.user.entity.User) authentication.getPrincipal()).getId();
        }
        throw new com.interviai.backend.common.exception.BusinessException("User not authenticated properly");
    }
}