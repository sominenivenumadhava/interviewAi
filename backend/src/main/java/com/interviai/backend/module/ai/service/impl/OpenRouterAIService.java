package com.interviai.backend.module.ai.service.impl;

import com.interviai.backend.module.ai.config.OpenRouterConfig;
import com.interviai.backend.module.ai.dto.OpenRouterRequest;
import com.interviai.backend.module.ai.dto.OpenRouterResponse;
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
public class OpenRouterAIService implements AIService {

    private static final Logger log = LoggerFactory.getLogger(OpenRouterAIService.class);

    @Autowired
    private WebClient openRouterWebClient;

    @Autowired
    private OpenRouterConfig openRouterConfig;

    @Override
    public Mono<String> generateContent(String prompt) {
        return generateContent(prompt, OpenRouterRequest.GenerationConfig.defaultConfig());
    }

    @Override
    public Mono<String> generateContent(String prompt, OpenRouterRequest.GenerationConfig config) {
        OpenRouterRequest request = new OpenRouterRequest(
                openRouterConfig.getDefaultModel(),
                prompt,
                config
        );
        return generateRawContent(request)
                .flatMap(response -> Mono.justOrEmpty(response.getFirstText()))
                .filter(text -> text != null && !text.isBlank())
                .switchIfEmpty(Mono.error(new AIServiceException("OpenRouter returned no content")));
    }

    @Override
    public Mono<String> generateStructuredContent(String prompt, String systemInstruction) {
        String fullPrompt = systemInstruction + "\n\n" + prompt;
        return generateContent(fullPrompt, OpenRouterRequest.GenerationConfig.preciseConfig());
    }

    @Override
    public Mono<String> generateCreativeStructuredContent(String prompt, String systemInstruction) {
        String fullPrompt = systemInstruction + "\n\n" + prompt;
        return generateContent(fullPrompt, OpenRouterRequest.GenerationConfig.creativeConfig());
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
            "You are a resume analyzer. Extract all technical and soft skills from the resume.\n\n" +
            "Return the response in the following JSON format:\n" +
            "{\n" +
            "  \"technicalSkills\": [{\"name\": \"skill\", \"level\": \"BEGINNER/INTERMEDIATE/ADVANCED/EXPERT\"}],\n" +
            "  \"softSkills\": [\"skill1\", \"skill2\"],\n" +
            "  \"tools\": [\"tool1\", \"tool2\"],\n" +
            "  \"certifications\": [\"cert1\", \"cert2\"]\n" +
            "}\n\n" +
            "Infer skill levels from experience and context. Categorize skills appropriately.";

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
            "  \"missingSkills\": [{\"skill\": \"skill name\", \"importance\": \"CRITICAL/HIGH/MEDIUM/LOW\", " +
            "\"learningPath\": \"Suggested learning resources\"}],\n" +
            "  \"recommendations\": [\"recommendation1\", \"recommendation2\"],\n" +
            "  \"estimatedTimeToCloseGap\": \"X months\",\n" +
            "  \"roadmap\": [{\"phase\": 1, \"duration\": \"X weeks\", \"skills\": [\"skill1\"], " +
            "\"resources\": [\"resource1\"]}]\n" +
            "}\n\n" +
            "Provide actionable and realistic recommendations.";

        String prompt = String.format(
            "Current Skills: %s\n\nTarget Role: %s\n\nAnalyze skill gap now.",
            userSkills, targetRole
        );

        return generateStructuredContent(prompt, systemInstruction);
    }

    @Override
    public Mono<OpenRouterResponse> generateRawContent(OpenRouterRequest request) {
        return openRouterWebClient.post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OpenRouterResponse.class)
                .doOnError(WebClientResponseException.class, e ->
                    log.error("OpenRouter API error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString())
                )
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof WebClientResponseException &&
                                ((WebClientResponseException) throwable).getStatusCode().is5xxServerError()))
                .onErrorMap(WebClientResponseException.class, e ->
                    new AIServiceException("OpenRouter request failed: " + e.getMessage(), e))
                .onErrorMap(
                    e -> !(e instanceof AIServiceException),
                    e -> new AIServiceException("Unexpected error in OpenRouter service", e)
                );
    }
}
