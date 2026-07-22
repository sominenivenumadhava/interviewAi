package com.interviai.backend.module.analytics.controller;

import java.util.UUID;
import com.interviai.backend.common.dto.ApiResponse;
import java.util.UUID;
import com.interviai.backend.module.analytics.dto.CompanyAnalyticsResponse;
import java.util.UUID;
import com.interviai.backend.module.analytics.dto.UserAnalyticsResponse;
import java.util.UUID;
import com.interviai.backend.module.analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Analytics", description = "User performance analytics and insights APIs")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {
    
    @Autowired
    private AnalyticsService analyticsService;
    
    @GetMapping("/user")
    @Operation(summary = "Get comprehensive analytics for the current user")
    public ResponseEntity<ApiResponse<UserAnalyticsResponse>> getUserAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Authentication authentication) {
        UUID userId = getUserId(authentication);
        
        // Default to last 12 months if dates not provided
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate == null) {
            startDate = endDate.minusMonths(12);
        }
        
        UserAnalyticsResponse response = analyticsService.getUserAnalytics(userId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/company/{company}")
    @Operation(summary = "Get analytics for a specific company")
    public ResponseEntity<ApiResponse<CompanyAnalyticsResponse>> getCompanyAnalytics(
            @PathVariable String company,
            Authentication authentication) {
        UUID userId = getUserId(authentication);
        CompanyAnalyticsResponse response = analyticsService.getCompanyAnalytics(userId, company);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/role-comparison")
    @Operation(summary = "Get performance comparison across different roles")
    public ResponseEntity<ApiResponse<List<UserAnalyticsResponse.CategoryPerformance>>> getRoleComparison(
            Authentication authentication) {
        UUID userId = getUserId(authentication);
        List<UserAnalyticsResponse.CategoryPerformance> response = analyticsService.getRoleComparison(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/skill-progress")
    @Operation(summary = "Get skill progress over time")
    public ResponseEntity<ApiResponse<List<UserAnalyticsResponse.SkillProgress>>> getSkillProgress(
            @RequestParam(required = false) String skillName,
            Authentication authentication) {
        UUID userId = getUserId(authentication);
        List<UserAnalyticsResponse.SkillProgress> response = analyticsService.getSkillProgress(userId, skillName);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/preparation-insights/{targetRole}")
    @Operation(summary = "Get interview preparation insights for a target role")
    public ResponseEntity<ApiResponse<AnalyticsService.PreparationInsights>> getPreparationInsights(
            @PathVariable String targetRole,
            Authentication authentication) {
        UUID userId = getUserId(authentication);
        AnalyticsService.PreparationInsights response = analyticsService.getPreparationInsights(userId, targetRole);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/export")
    @Operation(summary = "Export analytics data in various formats")
    public ResponseEntity<byte[]> exportAnalytics(
            @RequestParam(defaultValue = "pdf") String format,
            Authentication authentication) {
        UUID userId = getUserId(authentication);
        byte[] exportData = analyticsService.exportAnalytics(userId, format);
        
        String filename = String.format("analytics-report-%s.%s", 
                LocalDate.now().toString(), format.toLowerCase());
        
        MediaType contentType = switch (format.toLowerCase()) {
            case "csv" -> MediaType.parseMediaType("text/csv");
            case "pdf" -> MediaType.APPLICATION_PDF;
            case "json" -> MediaType.APPLICATION_JSON;
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(contentType)
                .body(exportData);
    }
    
    private UUID getUserId(Authentication authentication) {
        if (authentication.getPrincipal() instanceof com.interviai.backend.module.user.entity.User) {
            return ((com.interviai.backend.module.user.entity.User) authentication.getPrincipal()).getId();
        }
        throw new com.interviai.backend.common.exception.BusinessException("User not authenticated properly");
    }
}
