package com.interviai.backend.module.evaluation.controller;

import java.util.UUID;
import com.interviai.backend.common.dto.ApiResponse;
import java.util.UUID;
import com.interviai.backend.module.evaluation.dto.InterviewEvaluationResponse;
import java.util.UUID;
import com.interviai.backend.module.evaluation.dto.SkillGapAnalysisResponse;
import java.util.UUID;
import com.interviai.backend.module.evaluation.service.EvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/evaluations")
@Tag(name = "Evaluation", description = "Interview evaluation and skill gap analysis APIs")
@SecurityRequirement(name = "bearerAuth")
public class EvaluationController {
    
    @Autowired
    private EvaluationService evaluationService;
    
    @GetMapping("/interview/{sessionId}")
    @Operation(summary = "Generate comprehensive evaluation for a completed interview")
    public ResponseEntity<ApiResponse<InterviewEvaluationResponse>> generateInterviewEvaluation(
            @PathVariable String sessionId,
            Authentication authentication) {
        UUID userId = getUserId(authentication);
        InterviewEvaluationResponse response = evaluationService.generateInterviewEvaluation(sessionId, userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Evaluation generated successfully"));
    }
    
    @GetMapping("/skill-gap")
    @Operation(summary = "Get skill gap analysis for a target role")
    public ResponseEntity<ApiResponse<SkillGapAnalysisResponse>> getSkillGapAnalysis(
            @RequestParam String targetRole,
            Authentication authentication) {
        UUID userId = getUserId(authentication);
        SkillGapAnalysisResponse response = evaluationService.getSkillGapAnalysis(userId, targetRole);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/performance-trends")
    @Operation(summary = "Get performance trends for the user")
    public ResponseEntity<ApiResponse<SkillGapAnalysisResponse.PerformanceTrends>> getPerformanceTrends(
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {
        UUID userId = getUserId(authentication);
        SkillGapAnalysisResponse.PerformanceTrends response = evaluationService.getPerformanceTrends(userId, limit);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/benchmark/{role}")
    @Operation(summary = "Compare user's performance against role benchmarks")
    public ResponseEntity<ApiResponse<InterviewEvaluationResponse.BenchmarkComparison>> getBenchmarkComparison(
            @PathVariable String role,
            Authentication authentication) {
        UUID userId = getUserId(authentication);
        InterviewEvaluationResponse.BenchmarkComparison response = evaluationService.getBenchmarkComparison(userId, role);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/interview/{sessionId}/report")
    @Operation(summary = "Download interview evaluation report")
    public ResponseEntity<byte[]> downloadInterviewReport(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "pdf") String format,
            Authentication authentication) {
        UUID userId = getUserId(authentication);
        byte[] report = evaluationService.generateInterviewReport(sessionId, userId, format);
        
        String filename = String.format("interview-report-%s.%s", sessionId, format);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(format.equals("pdf") ? MediaType.APPLICATION_PDF : MediaType.APPLICATION_JSON)
                .body(report);
    }
    
    @GetMapping("/practice-questions")
    @Operation(summary = "Get recommended practice questions based on skill gaps")
    public ResponseEntity<ApiResponse<List<String>>> getRecommendedPracticeQuestions(
            @RequestParam String role,
            @RequestParam(defaultValue = "10") int count,
            Authentication authentication) {
        UUID userId = getUserId(authentication);
        List<String> questions = evaluationService.getRecommendedPracticeQuestions(userId, role, count);
        return ResponseEntity.ok(ApiResponse.success(questions));
    }
    
    private UUID getUserId(Authentication authentication) {
        if (authentication.getPrincipal() instanceof com.interviai.backend.module.user.entity.User) {
            return ((com.interviai.backend.module.user.entity.User) authentication.getPrincipal()).getId();
        }
        throw new com.interviai.backend.common.exception.BusinessException("User not authenticated properly");
    }
}
