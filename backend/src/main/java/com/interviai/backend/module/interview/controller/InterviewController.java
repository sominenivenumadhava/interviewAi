package com.interviai.backend.module.interview.controller;

import com.interviai.backend.common.dto.ApiResponse;
import com.interviai.backend.common.dto.PageRequest;
import com.interviai.backend.common.dto.PageResponse;
import com.interviai.backend.module.interview.dto.request.CreateInterviewRequest;
import com.interviai.backend.module.interview.dto.request.SubmitAnswerRequest;
import com.interviai.backend.module.interview.dto.request.UpdateInterviewStatusRequest;
import com.interviai.backend.module.interview.dto.response.InterviewAnswerResponse;
import com.interviai.backend.module.interview.dto.response.InterviewQuestionResponse;
import com.interviai.backend.module.interview.dto.response.InterviewResponse;
import com.interviai.backend.module.interview.dto.response.InterviewSessionResponse;
import com.interviai.backend.module.interview.service.InterviewQuestionService;
import com.interviai.backend.module.interview.service.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/interviews")
@Tag(name = "Interview", description = "Interview management APIs")
@SecurityRequirement(name = "bearerAuth")
public class InterviewController {
    
    @Autowired
    private InterviewService interviewService;
    
    @Autowired
    private InterviewQuestionService questionService;
    
    @PostMapping
    @Operation(summary = "Create a new interview session")
    public ResponseEntity<ApiResponse<InterviewResponse>> createInterview(
            @Valid @RequestBody CreateInterviewRequest request,
            Authentication authentication) {
        UUID userId = getUserId(authentication);
        InterviewResponse response = interviewService.createInterview(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Interview created successfully"));
    }
    
    @PostMapping("/{sessionId}/start")
    @Operation(summary = "Start an interview session")
    public ResponseEntity<ApiResponse<InterviewSessionResponse>> startInterview(
            @PathVariable String sessionId,
            Authentication authentication) {
        UUID userId = getUserId(authentication);
        InterviewSessionResponse response = interviewService.startInterview(sessionId, userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Interview started successfully"));
    }
    
    @PutMapping("/status")
    @Operation(summary = "Update interview status")
    public ResponseEntity<ApiResponse<InterviewResponse>> updateInterviewStatus(
            @Valid @RequestBody UpdateInterviewStatusRequest request,
            Authentication authentication) {
        UUID userId = getUserId(authentication);
        InterviewResponse response = interviewService.updateInterviewStatus(request, userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Interview status updated successfully"));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get interview details by ID")
    public ResponseEntity<ApiResponse<InterviewResponse>> getInterview(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = getUserId(authentication);
        InterviewResponse response = interviewService.getInterview(id, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/session/{sessionId}")
    @Operation(summary = "Get interview session details")
    public ResponseEntity<ApiResponse<InterviewSessionResponse>> getInterviewSession(
            @PathVariable String sessionId,
            Authentication authentication) {
        UUID userId = getUserId(authentication);
        InterviewSessionResponse response = interviewService.getInterviewSession(sessionId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping
    @Operation(summary = "Get user's interviews with pagination")
    public ResponseEntity<ApiResponse<PageResponse<InterviewResponse>>> getUserInterviews(
            @Valid PageRequest pageRequest,
            Authentication authentication) {
        UUID userId = getUserId(authentication);
        PageResponse<InterviewResponse> response = interviewService.getUserInterviews(userId, pageRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming interviews")
    public ResponseEntity<ApiResponse<List<InterviewResponse>>> getUpcomingInterviews(
            Authentication authentication) {
        UUID userId = getUserId(authentication);
        List<InterviewResponse> response = interviewService.getUpcomingInterviews(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/recent")
    @Operation(summary = "Get recent completed interviews")
    public ResponseEntity<ApiResponse<List<InterviewResponse>>> getRecentInterviews(
            @RequestParam(defaultValue = "5") int limit,
            Authentication authentication) {
        UUID userId = getUserId(authentication);
        List<InterviewResponse> response = interviewService.getRecentInterviews(userId, limit);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an interview")
    public ResponseEntity<ApiResponse<Void>> deleteInterview(
            @PathVariable UUID id,
            Authentication authentication) {
        UUID userId = getUserId(authentication);
        interviewService.deleteInterview(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Interview deleted successfully"));
    }
    
    @PostMapping("/{sessionId}/complete")
    @Operation(summary = "Complete an interview session")
    public ResponseEntity<ApiResponse<InterviewResponse>> completeInterview(
            @PathVariable String sessionId,
            Authentication authentication) {
        UUID userId = getUserId(authentication);
        InterviewResponse response = interviewService.completeInterview(sessionId, userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Interview completed successfully"));
    }
    
    @PostMapping("/{sessionId}/pause")
    @Operation(summary = "Pause an interview session")
    public ResponseEntity<ApiResponse<InterviewResponse>> pauseInterview(
            @PathVariable String sessionId,
            Authentication authentication) {
        UUID userId = getUserId(authentication);
        InterviewResponse response = interviewService.pauseInterview(sessionId, userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Interview paused successfully"));
    }
    
    @PostMapping("/{sessionId}/resume")
    @Operation(summary = "Resume a paused interview")
    public ResponseEntity<ApiResponse<InterviewResponse>> resumeInterview(
            @PathVariable String sessionId,
            Authentication authentication) {
        UUID userId = getUserId(authentication);
        InterviewResponse response = interviewService.resumeInterview(sessionId, userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Interview resumed successfully"));
    }
    
    @PostMapping("/{sessionId}/cancel")
    @Operation(summary = "Cancel an interview session")
    public ResponseEntity<ApiResponse<InterviewResponse>> cancelInterview(
            @PathVariable String sessionId,
            Authentication authentication) {
        UUID userId = getUserId(authentication);
        InterviewResponse response = interviewService.cancelInterview(sessionId, userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Interview cancelled successfully"));
    }
    
    // Question-related endpoints
    @GetMapping("/{sessionId}/questions")
    @Operation(summary = "Get all questions for an interview")
    public ResponseEntity<ApiResponse<List<InterviewQuestionResponse>>> getAllQuestions(
            @PathVariable String sessionId,
            Authentication authentication) {
        UUID userId = getUserId(authentication);
        List<InterviewQuestionResponse> response = questionService.getAllQuestions(sessionId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/{sessionId}/questions/{questionOrder}")
    @Operation(summary = "Get a specific question")
    public ResponseEntity<ApiResponse<InterviewQuestionResponse>> getQuestion(
            @PathVariable String sessionId,
            @PathVariable Integer questionOrder,
            Authentication authentication) {
        UUID userId = getUserId(authentication);
        InterviewQuestionResponse response = questionService.getQuestion(sessionId, questionOrder, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/answer")
    @Operation(summary = "Submit an answer to a question")
    public ResponseEntity<ApiResponse<InterviewAnswerResponse>> submitAnswer(
            @Valid @RequestBody SubmitAnswerRequest request,
            Authentication authentication) {
        UUID userId = getUserId(authentication);
        InterviewAnswerResponse response = questionService.submitAnswer(request, userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Answer submitted successfully"));
    }
    
    @PostMapping("/{sessionId}/questions/{questionOrder}/evaluate")
    @Operation(summary = "Evaluate a specific answer")
    public ResponseEntity<ApiResponse<InterviewAnswerResponse>> evaluateAnswer(
            @PathVariable String sessionId,
            @PathVariable Integer questionOrder,
            Authentication authentication) {
        UUID userId = getUserId(authentication);
        InterviewAnswerResponse response = questionService.evaluateAnswer(sessionId, questionOrder, userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Answer evaluated successfully"));
    }
    
    @PostMapping("/{sessionId}/evaluate-all")
    @Operation(summary = "Evaluate all submitted answers")
    public ResponseEntity<ApiResponse<List<InterviewAnswerResponse>>> evaluateAllAnswers(
            @PathVariable String sessionId,
            Authentication authentication) {
        UUID userId = getUserId(authentication);
        List<InterviewAnswerResponse> response = questionService.evaluateAllAnswers(sessionId, userId);
        return ResponseEntity.ok(ApiResponse.success(response, "All answers evaluated successfully"));
    }
    
    @GetMapping("/{sessionId}/next-question")
    @Operation(summary = "Get the next unanswered question")
    public ResponseEntity<ApiResponse<InterviewQuestionResponse>> getNextQuestion(
            @PathVariable String sessionId,
            Authentication authentication) {
        UUID userId = getUserId(authentication);
        InterviewQuestionResponse response = questionService.getNextQuestion(sessionId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    private UUID getUserId(Authentication authentication) {
        if (authentication.getPrincipal() instanceof com.interviai.backend.module.user.entity.User) {
            return ((com.interviai.backend.module.user.entity.User) authentication.getPrincipal()).getId();
        }
        throw new com.interviai.backend.common.exception.BusinessException("User not authenticated properly");
    }
}
