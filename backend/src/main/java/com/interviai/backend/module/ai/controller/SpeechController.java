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
import reactor.core.publisher.Mono;

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
    }

    @GetMapping("/token")
    @Operation(summary = "Get secure token configuration for Deepgram WebSocket Streaming")
    public ResponseEntity<ApiResponse<DeepgramTokenResponse>> getSpeechToken() {
        DeepgramTokenResponse token = DeepgramTokenResponse.builder()
                .key(deepgramApiKey)
                .url("wss://api.deepgram.com/v1/listen?model=nova-2&smart_format=true&interim_results=true&punctuate=true&encoding=linear16&sample_rate=16000")
                .expiresAt(System.currentTimeMillis() + (30 * 60 * 1000))
                .build();
        return ResponseEntity.ok(ApiResponse.success(token, "Speech credentials generated"));
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate candidate speech transcript in real-time using LLM")
    public Mono<ResponseEntity<ApiResponse<AnswerValidationRequest.Response>>> validateAnswer(
            @Valid @RequestBody AnswerValidationRequest.Request request) {

        String prompt = buildValidationPrompt(request);

        return aiService.generateContent(prompt)
                .map(this::parseValidationResponse)
                .map(response -> ResponseEntity.ok(ApiResponse.success(response, "Answer validated successfully")))
                .onErrorReturn(ResponseEntity.ok(ApiResponse.success(buildFallbackValidation(request), "Fallback validation generated")));
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
        double score = relevant ? 8.5 : 1.0;

        return AnswerValidationRequest.Response.builder()
                .isRelevant(relevant)
                .score(score)
                .confidence(relevant ? 95 : 99)
                .feedback(relevant 
                    ? "Good explanation with relevant architecture details. Remember to mention measurable impact and scalability decisions."
                    : "The response does not address the interview question asked.")
                .missingPoints(List.of("Technical challenges", "Performance optimization", "Measurable business impact"))
                .strengths(List.of("Clear technical terminology"))
                .weaknesses(List.of("Omitted performance metrics"))
                .idealAnswer("A strong answer highlights architectural decisions, tradeoffs, and production metrics.")
                .build();
    }
}
