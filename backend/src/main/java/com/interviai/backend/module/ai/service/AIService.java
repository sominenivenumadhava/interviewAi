package com.interviai.backend.module.ai.service;

import com.interviai.backend.module.ai.dto.OpenRouterRequest;
import com.interviai.backend.module.ai.dto.OpenRouterResponse;
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
    Mono<String> generateContent(String prompt, OpenRouterRequest.GenerationConfig config);
    
    /**
     * Generate structured content with a specific format
     * @param prompt The user prompt
     * @param systemInstruction System instructions for the AI
     * @return AI generated response
     */
    Mono<String> generateStructuredContent(String prompt, String systemInstruction);

    /**
     * Generate structured content with high temperature for interview question variety.
     */
    Mono<String> generateCreativeStructuredContent(String prompt, String systemInstruction);
    
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
     * Get the raw OpenRouter chat-completion response
     * @param request Custom OpenRouter request
     * @return Raw OpenRouter response
     */
    Mono<OpenRouterResponse> generateRawContent(OpenRouterRequest request);
}
