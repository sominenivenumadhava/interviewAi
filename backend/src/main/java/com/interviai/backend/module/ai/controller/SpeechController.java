package com.interviai.backend.module.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviai.backend.common.dto.ApiResponse;
import com.interviai.backend.module.ai.dto.AnswerValidationRequest;
import com.interviai.backend.module.ai.service.AIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/api/v1/speech")
@Tag(name = "Speech & Answer Validation", description = "Speech-to-Text token and real-time LLM answer validation APIs")
@SecurityRequirement(name = "bearerAuth")
public class SpeechController {

    private static final Logger log = LoggerFactory.getLogger(SpeechController.class);

    @Autowired
    private AIService aiService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${deepgram.api.key:mock-deepgram-key-dev-proxy}")
    private String deepgramApiKey;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeepgramTokenResponse {
        private String key;
        private String url;
        private Long expiresAt;
        private boolean available;
        private String provider;
        private String message;
    }

    @GetMapping("/token")
    @Operation(summary = "Get secure token configuration for Deepgram WebSocket Streaming")
    public ResponseEntity<ApiResponse<DeepgramTokenResponse>> getSpeechToken() {
        // Never return the master Deepgram API key to the client.
        // Until temporary-token minting exists, always fall back to browser speech.
        boolean mockOrBlank = deepgramApiKey == null
                || deepgramApiKey.isBlank()
                || deepgramApiKey.startsWith("mock-");

        String message = mockOrBlank
                ? "Browser speech will be used. Deepgram API key is not configured."
                : "Browser speech will be used. Deepgram temporary-token minting is not yet available.";

        DeepgramTokenResponse token = DeepgramTokenResponse.builder()
                .key(null)
                .url(null)
                .expiresAt(null)
                .available(false)
                .provider("browser")
                .message(message)
                .build();
        return ResponseEntity.ok(ApiResponse.success(token, "Browser speech fallback configured"));
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate candidate speech transcript in real-time using LLM")
    public ResponseEntity<ApiResponse<AnswerValidationRequest.Response>> validateAnswer(
            @Valid @RequestBody AnswerValidationRequest.Request request) {

        String prompt = buildValidationPrompt(request);

        try {
            AnswerValidationRequest.Response response = aiService.generateContent(prompt)
                    .map(this::parseValidationResponse)
                    .block(Duration.ofSeconds(12));

            if (response != null) {
                return ResponseEntity.ok(ApiResponse.success(response, "Answer validated successfully"));
            }
        } catch (Exception e) {
            /*
             * This application uses Spring MVC with stateless JWT security. Returning
             * a Mono from this controller caused an async servlet redispatch after the
             * AI call; that redispatch lost the SecurityContext and converted a valid
             * request into a misleading 401. Complete the bounded AI call inside the
             * original authenticated request and preserve the existing fallback.
             */
            log.warn("Answer validation AI call failed, using fallback: {}", e.getMessage());
        }

        return ResponseEntity.ok(
                ApiResponse.success(buildFallbackValidation(request), "Fallback validation generated"));
    }

    private String buildValidationPrompt(AnswerValidationRequest.Request req) {
        return String.format("""
            You are a Senior Technical Interviewer evaluating a candidate's response in real time.
            
            INTERVIEW CONTEXT:
            Target Role: %s
            Target Company: %s
            Interview Type: %s
            
            QUESTION ASKED:
            "%s"
            
            CANDIDATE TRANSCRIPT:
            "%s"
            
            INSTRUCTIONS & VALIDATION CRITERIA:
            1. First check: Does the candidate transcript ACTUALLY answer the question asked?
               - If candidate talks about an unrelated topic (e.g. Q: "What is Kafka?", A: "I like Java"), set isRelevant = false, score = 1.0, confidence = 99.
            2. Evaluate on 10-point scale: Technical Accuracy, Completeness, Communication Clarity, Real Interview Quality.
            3. Identify missing key points, strengths, and weaknesses.
            
            RETURN JSON ONLY with no markdown wrapping:
            {
              "isRelevant": true,
              "score": 8.5,
              "confidence": 95,
              "feedback": "Concise feedback explanation...",
              "missingPoints": ["Technical challenges", "Performance optimization", "Measurable impact"],
              "strengths": ["Clear explanation", "Good terminology"],
              "weaknesses": ["Omitted scalability decisions"],
              "idealAnswer": "Key points of ideal answer"
            }
            """,
            req.getRole() != null ? req.getRole() : "Software Engineer",
            req.getCompany() != null ? req.getCompany() : "Tech Company",
            req.getInterviewType() != null ? req.getInterviewType() : "TECHNICAL",
            req.getQuestion(),
            req.getTranscript()
        );
    }

    private AnswerValidationRequest.Response parseValidationResponse(String json) {
        try {
            String clean = json.trim();
            if (clean.startsWith("```json")) clean = clean.substring(7);
            if (clean.startsWith("```")) clean = clean.substring(3);
            if (clean.endsWith("```")) clean = clean.substring(0, clean.length() - 3);
            return objectMapper.readValue(clean.trim(), AnswerValidationRequest.Response.class);
        } catch (Exception e) {
            log.warn("Failed to parse LLM validation JSON, using fallback: {}", json, e);
            return buildFallbackValidation(null);
        }
    }

    private AnswerValidationRequest.Response buildFallbackValidation(AnswerValidationRequest.Request req) {
        String transcript = req != null && req.getTranscript() != null ? req.getTranscript().toLowerCase() : "";
        boolean relevant = transcript.length() > 10;
        double score = relevant ? 5.0 : 1.0;

        return AnswerValidationRequest.Response.builder()
                .isRelevant(relevant)
                .score(score)
                .confidence(40)
                .feedback("[Fallback] AI scoring unavailable. "
                        + (relevant
                            ? "A provisional mid-range score was assigned based on transcript length only; re-run when AI scoring is available."
                            : "The response appears too short or empty to evaluate; re-run when AI scoring is available."))
                .missingPoints(List.of("Technical challenges", "Performance optimization", "Measurable business impact"))
                .strengths(List.of())
                .weaknesses(List.of("AI scoring unavailable — results are provisional"))
                .idealAnswer("A strong answer highlights architectural decisions, tradeoffs, and production metrics.")
                .build();
    }
}
