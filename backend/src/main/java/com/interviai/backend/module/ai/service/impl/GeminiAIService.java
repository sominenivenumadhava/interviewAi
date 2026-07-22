package com.interviai.backend.module.ai.service.impl;

import com.interviai.backend.module.ai.config.GeminiConfig;
import com.interviai.backend.module.ai.dto.GeminiRequest;
import com.interviai.backend.module.ai.dto.GeminiResponse;
import com.interviai.backend.module.ai.exception.AIServiceException;
import com.interviai.backend.module.ai.service.AIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
public class GeminiAIService implements AIService {
    
    private static final Logger log = LoggerFactory.getLogger(GeminiAIService.class);
    
    @Autowired
    private WebClient geminiWebClient;
    
    @Autowired
    private GeminiConfig geminiConfig;
    
    @Override
    public Mono<String> generateContent(String prompt) {
        return generateContent(prompt, GeminiRequest.GenerationConfig.defaultConfig());
    }
    
    @Override
    public Mono<String> generateContent(String prompt, GeminiRequest.GenerationConfig config) {
        GeminiRequest request = new GeminiRequest(prompt, config);
        return generateRawContent(request)
                .map(GeminiResponse::getFirstText)
                .switchIfEmpty(Mono.error(new AIServiceException("No response generated")));
    }
    
    @Override
    public Mono<String> generateStructuredContent(String prompt, String systemInstruction) {
        String fullPrompt = systemInstruction + "\n\n" + prompt;
        GeminiRequest.GenerationConfig config = GeminiRequest.GenerationConfig.preciseConfig();
        return generateContent(fullPrompt, config);
    }
    
    @Override
    public Mono<String> generateInterviewQuestions(String resumeText, String role, String difficulty, int count) {
        String systemInstruction = String.format(
            "You are an expert technical interviewer. Based on the following resume and requirements, " +
            "generate %d %s-level interview questions for a %s position.\n\n" +
            "Return the response in the following JSON format:\n" +
            "{\n" +
            "  \"questions\": [\n" +
            "    {\n" +
            "      \"id\": \"q1\",\n" +
            "      \"question\": \"...\",\n" +
            "      \"category\": \"technical/behavioral/situational\",\n" +
            "      \"difficulty\": \"%s\",\n" +
            "      \"expectedTimeMinutes\": 5,\n" +
            "      \"evaluationCriteria\": [\"criteria1\", \"criteria2\"],\n" +
            "      \"followUpQuestions\": [\"followup1\", \"followup2\"]\n" +
            "    }\n" +
            "  ]\n" +
            "}\n\n" +
            "Ensure questions are relevant to the candidate's experience level and the role requirements.",
            count, difficulty, role, difficulty
        );
        
        String prompt = "Resume:\n" + resumeText + "\n\nGenerate interview questions now.";
        
        return generateStructuredContent(prompt, systemInstruction);
    }
    
    @Override
    public Mono<String> evaluateAnswer(String question, String answer, String expectedCriteria) {
        String systemInstruction = 
            "You are an expert interview evaluator. Evaluate the following answer based on the question and criteria.\n\n" +
            "Return the response in the following JSON format:\n" +
            "{\n" +
            "  \"score\": 0-100,\n" +
            "  \"rating\": \"EXCELLENT/GOOD/SATISFACTORY/NEEDS_IMPROVEMENT/POOR\",\n" +
            "  \"strengths\": [\"strength1\", \"strength2\"],\n" +
            "  \"improvements\": [\"improvement1\", \"improvement2\"],\n" +
            "  \"feedback\": \"Detailed feedback text\",\n" +
            "  \"suggestedAnswer\": \"An example of a strong answer\"\n" +
            "}\n\n" +
            "Be constructive and specific in your feedback.";
        
        String prompt = String.format(
            "Question: %s\n\nAnswer: %s\n\nEvaluation Criteria: %s\n\nEvaluate the answer now.",
            question, answer, expectedCriteria
        );
        
        return generateStructuredContent(prompt, systemInstruction);
    }
    
    @Override
    public Mono<String> extractSkills(String resumeText) {
        String systemInstruction = 
            "You are an expert resume analyzer. Extract all technical and soft skills from the resume.\n\n" +
            "Return the response in the following JSON format:\n" +
            "{\n" +
            "  \"technicalSkills\": [\n" +
            "    {\n" +
            "      \"category\": \"Programming Languages/Frameworks/Tools/Databases\",\n" +
            "      \"skills\": [\"skill1\", \"skill2\"],\n" +
            "      \"proficiencyLevel\": \"EXPERT/ADVANCED/INTERMEDIATE/BEGINNER\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"softSkills\": [\"skill1\", \"skill2\"],\n" +
            "  \"domainKnowledge\": [\"domain1\", \"domain2\"],\n" +
            "  \"certifications\": [\"cert1\", \"cert2\"]\n" +
            "}\n\n" +
            "Be comprehensive and categorize skills appropriately.";
        
        String prompt = "Resume:\n" + resumeText + "\n\nExtract all skills now.";
        
        return generateStructuredContent(prompt, systemInstruction);
    }
    
    @Override
    public Mono<String> analyzeSkillGap(String userSkills, String targetRole) {
        String systemInstruction = 
            "You are a career advisor. Analyze the skill gap between current skills and target role requirements.\n\n" +
            "Return the response in the following JSON format:\n" +
            "{\n" +
            "  \"matchPercentage\": 0-100,\n" +
            "  \"matchedSkills\": [\"skill1\", \"skill2\"],\n" +
            "  \"missingSkills\": [\n" +
            "    {\n" +
            "      \"skill\": \"skill name\",\n" +
            "      \"importance\": \"CRITICAL/HIGH/MEDIUM/LOW\",\n" +
            "      \"learningPath\": \"Suggested learning resources\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"recommendations\": [\"recommendation1\", \"recommendation2\"],\n" +
            "  \"estimatedTimeToCloseGap\": \"X months\",\n" +
            "  \"roadmap\": [\n" +
            "    {\n" +
            "      \"phase\": 1,\n" +
            "      \"duration\": \"X weeks\",\n" +
            "      \"skills\": [\"skill1\", \"skill2\"],\n" +
            "      \"resources\": [\"resource1\", \"resource2\"]\n" +
            "    }\n" +
            "  ]\n" +
            "}\n\n" +
            "Provide actionable and realistic recommendations.";
        
        String prompt = String.format(
            "Current Skills: %s\n\nTarget Role: %s\n\nAnalyze skill gap now.",
            userSkills, targetRole
        );
        
        return generateStructuredContent(prompt, systemInstruction);
    }
    
    @Override
    public Mono<GeminiResponse> generateRawContent(GeminiRequest request) {
        String endpoint = String.format("/v1beta/models/%s:generateContent", geminiConfig.getDefaultModel());
        
        return geminiWebClient.post()
                .uri(endpoint)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GeminiResponse.class)
                .doOnError(WebClientResponseException.class, e -> {
                    log.error("Gemini API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
                })
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof WebClientResponseException &&
                                ((WebClientResponseException) throwable).getStatusCode().is5xxServerError()))
                .onErrorMap(WebClientResponseException.class, e -> 
                    new AIServiceException("Failed to generate content: " + e.getMessage(), e))
                .onErrorMap(Exception.class, e -> 
                    new AIServiceException("Unexpected error in AI service", e));
    }
}