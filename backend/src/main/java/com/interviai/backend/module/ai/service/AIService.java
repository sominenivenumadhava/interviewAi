package com.interviai.backend.module.ai.service;

import com.interviai.backend.module.ai.dto.GeminiRequest;
import com.interviai.backend.module.ai.dto.GeminiResponse;
import reactor.core.publisher.Mono;

public interface AIService {
    
    /**
     * Generate content using the default model and configuration
     * @param prompt The user prompt
     * @return AI generated response
     */
    Mono<String> generateContent(String prompt);
    
    /**
     * Generate content with custom configuration
     * @param prompt The user prompt
     * @param config Generation configuration
     * @return AI generated response
     */
    Mono<String> generateContent(String prompt, GeminiRequest.GenerationConfig config);
    
    /**
     * Generate structured content with a specific format
     * @param prompt The user prompt
     * @param systemInstruction System instructions for the AI
     * @return AI generated response
     */
    Mono<String> generateStructuredContent(String prompt, String systemInstruction);
    
    /**
     * Generate interview questions based on resume
     * @param resumeText Resume content
     * @param role Target job role
     * @param difficulty Difficulty level (EASY, MEDIUM, HARD)
     * @param count Number of questions
     * @return Generated questions in JSON format
     */
    Mono<String> generateInterviewQuestions(String resumeText, String role, String difficulty, int count);
    
    /**
     * Evaluate interview answer
     * @param question The question
     * @param answer User's answer
     * @param expectedCriteria Evaluation criteria
     * @return Evaluation result in JSON format
     */
    Mono<String> evaluateAnswer(String question, String answer, String expectedCriteria);
    
    /**
     * Extract skills from resume text
     * @param resumeText Resume content
     * @return Extracted skills in JSON format
     */
    Mono<String> extractSkills(String resumeText);
    
    /**
     * Generate skill gap analysis
     * @param userSkills User's current skills
     * @param targetRole Target job role
     * @return Skill gap analysis in JSON format
     */
    Mono<String> analyzeSkillGap(String userSkills, String targetRole);
    
    /**
     * Get raw Gemini API response
     * @param request Custom Gemini request
     * @return Raw Gemini response
     */
    Mono<GeminiResponse> generateRawContent(GeminiRequest request);
}