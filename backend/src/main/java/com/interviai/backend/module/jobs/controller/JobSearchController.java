package com.interviai.backend.module.jobs.controller;

import com.interviai.backend.common.dto.ApiResponse;
import com.interviai.backend.common.exception.BusinessException;
import com.interviai.backend.module.jobs.dto.*;
import com.interviai.backend.module.jobs.service.JobSearchService;
import com.interviai.backend.module.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
@Tag(name = "Job Search", description = "AI Job Search Finder APIs")
@SecurityRequirement(name = "bearerAuth")
public class JobSearchController {

    @Autowired
    private JobSearchService jobSearchService;

    @PostMapping("/search")
    @Operation(summary = "Start a job search scraping process")
    public ResponseEntity<ApiResponse<JobSearchResponse>> startSearch(
            @Valid @RequestBody JobSearchRequest request,
            Authentication authentication) {
        User user = getUser(authentication);
        JobSearchResponse response = jobSearchService.startSearch(request, user);
        return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
    }

    @GetMapping("/search/{searchId}/status")
    @Operation(summary = "Check status and progress of job search")
    public ResponseEntity<ApiResponse<JobSearchStatusResponse>> getStatus(
            @PathVariable UUID searchId,
            Authentication authentication) {
        User user = getUser(authentication);
        JobSearchStatusResponse response = jobSearchService.getSearchStatus(searchId, user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search/{searchId}/results")
    @Operation(summary = "Get final results of the job search")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getResults(
            @PathVariable UUID searchId,
            Authentication authentication) {
        User user = getUser(authentication);
        Map<String, Object> response = jobSearchService.getSearchResults(searchId, user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search/history")
    @Operation(summary = "Get search history of the user")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getHistory(
            Authentication authentication) {
        User user = getUser(authentication);
        List<Map<String, Object>> response = jobSearchService.getSearchHistory(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{jobId}/save")
    @Operation(summary = "Save a job listing")
    public ResponseEntity<ApiResponse<Void>> saveJob(
            @PathVariable String jobId,
            @RequestBody JobResultDto jobDto,
            Authentication authentication) {
        User user = getUser(authentication);
        jobSearchService.saveJob(jobId, jobDto, user);
        return ResponseEntity.ok(ApiResponse.success(null, "Job saved successfully"));
    }

    @DeleteMapping("/{jobId}/save")
    @Operation(summary = "Remove a saved job listing")
    public ResponseEntity<ApiResponse<Void>> removeSavedJob(
            @PathVariable String jobId,
            Authentication authentication) {
        User user = getUser(authentication);
        jobSearchService.removeSavedJob(jobId, user);
        return ResponseEntity.ok(ApiResponse.success(null, "Job removed from saved list"));
    }

    @GetMapping("/saved")
    @Operation(summary = "Get user's saved job listings")
    public ResponseEntity<ApiResponse<List<JobResultDto>>> getSavedJobs(
            Authentication authentication) {
        User user = getUser(authentication);
        List<JobResultDto> response = jobSearchService.getSavedJobs(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private User getUser(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        throw new BusinessException("User not authenticated");
    }
}
