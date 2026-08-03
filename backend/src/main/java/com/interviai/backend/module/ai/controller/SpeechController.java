package com.interviai.backend.module.ai.controller;

import com.fasterxml.jackson.databind.JsonNode;
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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/speech")
@Tag(name = "Speech & Answer Validation", description = "Speech-to-Text token and real-time LLM answer validation APIs")
@SecurityRequirement(name = "bearerAuth")
public class SpeechController {

    private static final Logger log = LoggerFactory.getLogger(SpeechController.class);
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    @Autowired
    private AIService aiService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${deepgram.api.key:}")
    private String deepgramApiKey;

    @Value("${deepgram.api.base-url:https://api.deepgram.com}")
    private String deepgramBaseUrl;

    @Value("${deepgram.api.listen-url:wss://api.deepgram.com/v1/listen?model=nova-2&smart_format=true&interim_results=true&punctuate=true&encoding=opus&container=webm}")
    private String deepgramListenUrl;

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
        /** "bearer" for temporary JWT, "token" for legacy API-key protocol */
        private String authScheme;
        private String message;
    }

    @GetMapping("/token")
    @Operation(summary = "Get secure temporary token for Deepgram WebSocket Streaming")
    public ResponseEntity<ApiResponse<DeepgramTokenResponse>> getSpeechToken() {
        boolean mockOrBlank = deepgramApiKey == null
                || deepgramApiKey.isBlank()
                || deepgramApiKey.startsWith("mock-")
                || deepgramApiKey.startsWith("your-")
                || "CHANGE_ME".equalsIgnoreCase(deepgramApiKey);

        if (mockOrBlank) {
            DeepgramTokenResponse fallback = DeepgramTokenResponse.builder()
                    .key(null)
                    .url(null)
                    .expiresAt(null)
                    .available(false)
                    .provider("browser")
                    .authScheme(null)
                    .message("Browser speech will be used. Set DEEPGRAM_API_KEY in .env and restart the backend.")
                    .build();
            return ResponseEntity.ok(ApiResponse.success(fallback, "Browser speech fallback configured"));
        }

        try {
            DeepgramTokenResponse token = mintDeepgramTemporaryToken();
            return ResponseEntity.ok(ApiResponse.success(token, "Deepgram temporary token issued"));
        } catch (InsufficientDeepgramPermissionsException e) {
            // Usage-scoped keys cannot call /auth/grant. Fall back to token-protocol
            // WebSocket auth for already-authenticated app users (endpoint requires JWT).
            log.warn("Deepgram grant forbidden for this API key; using direct token WebSocket auth. "
                    + "Create a Member-scoped key for short-lived JWTs. {}", e.getMessage());
            DeepgramTokenResponse token = DeepgramTokenResponse.builder()
                    .key(deepgramApiKey.trim())
                    .url(deepgramListenUrl)
                    .expiresAt(Instant.now().plusSeconds(3600).toEpochMilli())
                    .available(true)
                    .provider("deepgram")
                    .authScheme("token")
                    .message("Deepgram live STT ready (API key WebSocket auth)")
                    .build();
            return ResponseEntity.ok(ApiResponse.success(token, "Deepgram token configured"));
        } catch (Exception e) {
            log.warn("Failed to mint Deepgram temporary token, falling back to browser STT: {}", e.getMessage());
            DeepgramTokenResponse fallback = DeepgramTokenResponse.builder()
                    .key(null)
                    .url(null)
                    .expiresAt(null)
                    .available(false)
                    .provider("browser")
                    .authScheme(null)
                    .message("Deepgram token minting failed (" + e.getMessage() + "). Browser speech will be used.")
                    .build();
            return ResponseEntity.ok(ApiResponse.success(fallback, "Browser speech fallback configured"));
        }
    }

    private static class InsufficientDeepgramPermissionsException extends Exception {
        InsufficientDeepgramPermissionsException(String message) {
            super(message);
        }
    }

    /**
     * Mint a short-lived JWT via Deepgram /v1/auth/grant so the browser never
     * receives the master API key. JWT is valid ~30–60s for the WebSocket handshake.
     */
    private DeepgramTokenResponse mintDeepgramTemporaryToken() throws Exception {
        String grantUrl = deepgramBaseUrl.replaceAll("/$", "") + "/v1/auth/grant";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(grantUrl))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", "Token " + deepgramApiKey.trim())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"ttl_seconds\":60}"))
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 401 || response.statusCode() == 403) {
            throw new InsufficientDeepgramPermissionsException(
                    "Deepgram grant HTTP " + response.statusCode() + ": " + truncate(response.body(), 200));
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Deepgram grant HTTP " + response.statusCode() + ": "
                    + truncate(response.body(), 200));
        }

        JsonNode root = objectMapper.readTree(response.body());
        String accessToken = textOrNull(root, "access_token");
        if (accessToken == null || accessToken.isBlank()) {
            accessToken = textOrNull(root, "token");
        }
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException("Deepgram grant response missing access_token");
        }

        double expiresIn = root.has("expires_in") && root.get("expires_in").isNumber()
                ? root.get("expires_in").asDouble()
                : 60.0;
        long expiresAt = Instant.now().plusSeconds(Math.max(1, (long) Math.floor(expiresIn))).toEpochMilli();

        return DeepgramTokenResponse.builder()
                .key(accessToken)
                .url(deepgramListenUrl)
                .expiresAt(expiresAt)
                .available(true)
                .provider("deepgram")
                .authScheme("bearer")
                .message("Deepgram live STT ready")
                .build();
    }

    private static String textOrNull(JsonNode root, String field) {
        JsonNode node = root.get(field);
        return node != null && !node.isNull() ? node.asText() : null;
    }

    private static String truncate(String value, int max) {
        if (value == null) return "";
        return value.length() <= max ? value : value.substring(0, max) + "...";
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
