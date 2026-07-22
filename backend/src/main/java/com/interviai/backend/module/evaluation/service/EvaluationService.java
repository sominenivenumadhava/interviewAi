package com.interviai.backend.module.evaluation.service;

import java.util.UUID;
import com.interviai.backend.module.evaluation.dto.InterviewEvaluationResponse;
import java.util.UUID;
import com.interviai.backend.module.evaluation.dto.SkillGapAnalysisResponse;

import java.util.List;

public interface EvaluationService {
    
    /**
     * Generate comprehensive evaluation for a completed interview
     * @param sessionId Interview session ID
     * @param userId User ID
     * @return Detailed evaluation response
     */
    InterviewEvaluationResponse generateInterviewEvaluation(String sessionId, UUID userId);
    
    /**
     * Get skill gap analysis for a user based on target role
     * @param userId User ID
     * @param targetRole Target job role
     * @return Skill gap analysis with recommendations
     */
    SkillGapAnalysisResponse getSkillGapAnalysis(UUID userId, String targetRole);
    
    /**
     * Get performance trends for a user
     * @param userId User ID
     * @param limit Number of recent interviews to include
     * @return Performance trend analysis
     */
    SkillGapAnalysisResponse.PerformanceTrends getPerformanceTrends(UUID userId, int limit);
    
    /**
     * Compare user's performance against role benchmarks
     * @param userId User ID
     * @param role Job role
     * @return Benchmark comparison data
     */
    InterviewEvaluationResponse.BenchmarkComparison getBenchmarkComparison(UUID userId, String role);
    
    /**
     * Generate interview feedback report
     * @param sessionId Interview session ID
     * @param userId User ID
     * @return PDF or HTML report as byte array
     */
    byte[] generateInterviewReport(String sessionId, UUID userId, String format);
    
    /**
     * Get recommended interview questions based on skill gaps
     * @param userId User ID
     * @param role Target role
     * @param count Number of questions
     * @return List of recommended practice questions
     */
    List<String> getRecommendedPracticeQuestions(UUID userId, String role, int count);
}
