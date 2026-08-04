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

    @Value("${deepgram.api.listen-url:wss://api.deepgram.com/v1/listen?model=nova-2&encoding=linear16&sample_rate=16000&channels=1&punctuate=true&interim_results=true&smart_format=true&endpointing=300}")
    private String deepgramListenUrl;

    @jakarta.annotation.PostConstruct
    void resolveDeepgramKey() {
        if (deepgramApiKey == null || deepgramApiKey.isBlank()) {
            deepgramApiKey = firstNonBlank(
                    System.getenv("DEEPGRAM_API_KEY"),
                    System.getProperty("DEEPGRAM_API_KEY"),
                    System.getProperty("deepgram.api.key")
            );
        }
        boolean ready = deepgramApiKey != null
                && !deepgramApiKey.isBlank()
                && !deepgramApiKey.startsWith("mock-")
                && !deepgramApiKey.startsWith("your-");
        log.info("Deepgram STT configured: {} (key length={})",
                ready, ready ? deepgramApiKey.trim().length() : 0);
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v.trim();
            }
        }
        return null;
    }

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
                    .map(json -> parseValidationResponse(json, request))
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
        String round = req.getInterviewType() != null ? req.getInterviewType().trim().toUpperCase() : "TECHNICAL";
        String criteria = switch (round) {
            case "CODING", "DSA", "OA" ->
                "Correctness, Time Complexity, Space Complexity, Edge Cases, Communication, Optimization";
            case "SYSTEM_DESIGN", "SYSTEMDESIGN" ->
                "Architecture, Scalability, Trade-offs, Communication";
            case "HR", "BEHAVIORAL" ->
                "Communication, Confidence, Personality, Cultural Fit";
            case "MANAGERIAL", "BAR_RAISER" ->
                "Decision Making, Ownership, Leadership, Stakeholder Management";
            case "APTITUDE", "ASSESSMENT" ->
                "Accuracy, Logical Reasoning, Speed, Clarity of Approach";
            default ->
                "Core Concepts, Practical Knowledge, Problem Solving, Confidence";
        };

        return String.format("""
            You are evaluating a candidate answer for a %s interview round.
            
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
               - If unrelated, set isRelevant = false, score = 1.0, confidence = 99.
            2. Evaluate on a 10-point scale using ONLY these round criteria: %s.
            3. Identify missing key points, strengths, and weaknesses for THIS round type only.
            
            RETURN JSON ONLY with no markdown wrapping:
            {
              "isRelevant": true,
              "score": 8.5,
              "confidence": 95,
              "feedback": "Concise feedback explanation...",
              "missingPoints": ["..."],
              "strengths": ["..."],
              "weaknesses": ["..."],
              "idealAnswer": "Key points of ideal answer"
            }
            """,
            round,
            req.getRole() != null ? req.getRole() : "Software Engineer",
            req.getCompany() != null ? req.getCompany() : "Tech Company",
            round,
            req.getQuestion(),
            req.getTranscript(),
            criteria
        );
    }

    private AnswerValidationRequest.Response parseValidationResponse(
            String json,
            AnswerValidationRequest.Request request) {
        try {
            String clean = json.trim();
            if (clean.startsWith("```json")) clean = clean.substring(7);
            if (clean.startsWith("```")) clean = clean.substring(3);
            if (clean.endsWith("```")) clean = clean.substring(0, clean.length() - 3);
            AnswerValidationRequest.Response parsed =
                    objectMapper.readValue(clean.trim(), AnswerValidationRequest.Response.class);
            if (parsed != null) {
                return parsed;
            }
        } catch (Exception e) {
            log.warn("Failed to parse LLM validation JSON, using fallback: {}", truncate(json, 200));
        }
        return buildFallbackValidation(request);
    }

    private AnswerValidationRequest.Response buildFallbackValidation(AnswerValidationRequest.Request req) {
        if (req == null) {
            return AnswerValidationRequest.Response.builder()
                    .isRelevant(false)
                    .score(1.0)
                    .confidence(30)
                    .feedback("[Fallback] AI scoring unavailable and no answer context was provided.")
                    .missingPoints(List.of())
                    .strengths(List.of())
                    .weaknesses(List.of("AI scoring unavailable — results are provisional"))
                    .idealAnswer("")
                    .build();
        }

        String round = req.getInterviewType() != null ? req.getInterviewType().trim().toUpperCase() : "";
        String question = req.getQuestion() != null ? req.getQuestion() : "";
        String transcript = req.getTranscript() != null ? req.getTranscript().trim() : "";

        if (transcript.isBlank()) {
            return AnswerValidationRequest.Response.builder()
                    .isRelevant(false)
                    .score(1.0)
                    .confidence(90)
                    .feedback("No answer was captured. Speak or type a response before submitting.")
                    .missingPoints(List.of("Provide a complete answer to the question"))
                    .strengths(List.of())
                    .weaknesses(List.of("Empty response"))
                    .idealAnswer("")
                    .build();
        }

        if (isAptitudeRound(round) || looksLikeAptitudeQuestion(question)) {
            return scoreAptitudeHeuristically(question, transcript);
        }

        boolean relevant = transcript.length() > 10;
        String criteriaHint = switch (round) {
            case "CODING", "DSA", "OA" -> "approach, complexity, and edge cases";
            case "SYSTEM_DESIGN", "SYSTEMDESIGN" -> "architecture, scalability, and trade-offs";
            case "HR", "BEHAVIORAL" -> "STAR structure, communication, and examples";
            case "MANAGERIAL", "BAR_RAISER" -> "ownership, decisions, and leadership";
            default -> "concepts, accuracy, and clarity";
        };

        return AnswerValidationRequest.Response.builder()
                .isRelevant(relevant)
                .score(relevant ? 5.5 : 1.0)
                .confidence(45)
                .feedback("[Fallback] AI scoring unavailable (OpenRouter key missing or unreachable). "
                        + "A provisional score was assigned from answer length only. "
                        + "Set OPENROUTER_API_KEY in .env and restart the backend for real scoring.")
                .missingPoints(relevant
                        ? List.of("Add more detail covering " + criteriaHint)
                        : List.of("Answer the question that was asked"))
                .strengths(relevant ? List.of("Provided a non-empty response") : List.of())
                .weaknesses(List.of("AI scoring unavailable — results are provisional"))
                .idealAnswer("Re-run validation after configuring OPENROUTER_API_KEY for round-specific feedback.")
                .build();
    }

    private static boolean isAptitudeRound(String round) {
        return "APTITUDE".equals(round) || "ASSESSMENT".equals(round) || "OA".equals(round);
    }

    private static boolean looksLikeAptitudeQuestion(String question) {
        String q = question.toLowerCase();
        return q.contains("km/h") || q.contains("ratio") || q.contains("average")
                || q.contains("percent") || q.contains("train") || q.contains("ages")
                || q.contains("speed") || q.contains("probability");
    }

    /**
     * Offline aptitude scoring so correct numeric answers are not marked irrelevant
     * when the LLM is unavailable.
     */
    private AnswerValidationRequest.Response scoreAptitudeHeuristically(String question, String transcript) {
        java.util.regex.Matcher numMatcher = java.util.regex.Pattern
                .compile("(\\d+(?:\\.\\d+)?)")
                .matcher(transcript.replace(",", ""));
        java.util.List<Double> answerNumbers = new java.util.ArrayList<>();
        while (numMatcher.find()) {
            try {
                answerNumbers.add(Double.parseDouble(numMatcher.group(1)));
            } catch (NumberFormatException ignored) {
                // skip
            }
        }

        if (answerNumbers.isEmpty()) {
            return AnswerValidationRequest.Response.builder()
                    .isRelevant(transcript.length() > 8)
                    .score(3.0)
                    .confidence(55)
                    .feedback("[Fallback] AI scoring unavailable. Your answer did not include a clear numeric result. "
                            + "For aptitude questions, state the final number (and units).")
                    .missingPoints(List.of("Final numeric answer", "Brief calculation steps"))
                    .strengths(List.of())
                    .weaknesses(List.of("No clear numeric conclusion", "AI scoring unavailable"))
                    .idealAnswer("State the computed value with units, e.g. \"72 km/h\".")
                    .build();
        }

        Double expected = tryComputeAptitudeExpected(question);
        double userValue = answerNumbers.get(answerNumbers.size() - 1); // prefer last number (final answer)
        boolean matched = false;
        double score = 6.0;
        String feedback;

        if (expected != null) {
            double tol = Math.max(0.5, Math.abs(expected) * 0.05); // 5% or 0.5
            matched = Math.abs(userValue - expected) <= tol;
            // Also accept nearby STT errors (e.g. 74 vs 72)
            boolean near = Math.abs(userValue - expected) <= Math.max(2.0, Math.abs(expected) * 0.08);
            if (matched) {
                score = 9.5;
                feedback = String.format(
                        "[Fallback] AI scoring unavailable, but your answer (%.2f) matches the expected result (%.2f). "
                                + "Configure OPENROUTER_API_KEY for richer feedback.",
                        userValue, expected);
            } else if (near) {
                score = 8.0;
                feedback = String.format(
                        "[Fallback] AI scoring unavailable. Your answer (%.2f) is close to the expected %.2f "
                                + "(possible speech/rounding difference). Configure OPENROUTER_API_KEY for full evaluation.",
                        userValue, expected);
                matched = true;
            } else {
                score = 4.0;
                feedback = String.format(
                        "[Fallback] AI scoring unavailable. Expected about %.2f, but heard %.2f. "
                                + "Re-check the calculation. Set OPENROUTER_API_KEY for detailed AI feedback.",
                        expected, userValue);
            }
        } else {
            feedback = String.format(
                    "[Fallback] AI scoring unavailable. Detected numeric answer %.2f and treated it as an aptitude response. "
                            + "Set OPENROUTER_API_KEY for exact correctness checking.",
                    userValue);
            score = 7.0;
            matched = true;
        }

        return AnswerValidationRequest.Response.builder()
                .isRelevant(true)
                .score(score)
                .confidence(expected != null ? 70 : 50)
                .feedback(feedback)
                .missingPoints(matched
                        ? List.of()
                        : List.of("Correct final numeric value", "Show intermediate steps"))
                .strengths(matched
                        ? List.of("Provided a numeric answer", "Addressed the aptitude question")
                        : List.of("Attempted a numeric answer"))
                .weaknesses(List.of("AI scoring unavailable — heuristic/offline check used"))
                .idealAnswer(expected != null
                        ? ("Expected answer ≈ " + expected)
                        : "Provide the final numeric result with units.")
                .build();
    }

    /** Best-effort solver for common aptitude patterns used in practice rounds. */
    private Double tryComputeAptitudeExpected(String question) {
        if (question == null || question.isBlank()) {
            return null;
        }
        String q = question.toLowerCase();
        java.util.regex.Matcher m;

        // Train length L meters passes a pole in T seconds → speed km/h = (L/T) * 18/5
        m = java.util.regex.Pattern
                .compile("(?:train|object)?[^\\d]{0,40}?(\\d+(?:\\.\\d+)?)\\s*m(?:eters?)?[^\\d]{0,80}?(\\d+(?:\\.\\d+)?)\\s*seconds?")
                .matcher(q);
        if ((q.contains("pole") || q.contains("speed") || q.contains("km")) && m.find()) {
            double length = Double.parseDouble(m.group(1));
            double seconds = Double.parseDouble(m.group(2));
            if (seconds > 0) {
                return (length / seconds) * (18.0 / 5.0);
            }
        }

        // Ratio of ages A:B = r1:r2 and B is D years older → ages
        m = java.util.regex.Pattern
                .compile("ratio[^\\d]{0,20}(\\d+)\\s*:\\s*(\\d+)[^\\d]{0,60}(\\d+)\\s*years?\\s*older")
                .matcher(q);
        if (q.contains("age") && m.find()) {
            double r1 = Double.parseDouble(m.group(1));
            double r2 = Double.parseDouble(m.group(2));
            double diff = Double.parseDouble(m.group(3));
            if (r2 != r1) {
                // B - A = diff, A/B = r1/r2 → A = diff * r1 / (r2 - r1)
                return diff * r1 / (r2 - r1); // return A's age; caller compares last number loosely
            }
        }

        return null;
    }
}
