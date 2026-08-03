package com.interviai.backend.module.interview.generation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviai.backend.common.exception.BusinessException;
import com.interviai.backend.module.ai.service.AIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Orchestrates round-specific AI generation with category validation and typed fallbacks.
 */
@Service
public class RoundAwareQuestionGenerator {

    private static final Logger log = LoggerFactory.getLogger(RoundAwareQuestionGenerator.class);

    private final QuestionGeneratorFactory factory;
    private final AIService aiService;
    private final ObjectMapper objectMapper;

    public RoundAwareQuestionGenerator(QuestionGeneratorFactory factory, ObjectMapper objectMapper) {
        this.factory = factory;
        this.aiService = factory.aiService();
        this.objectMapper = objectMapper;
    }

    public List<GeneratedQuestionDraft> generate(QuestionGenerationContext context) {
        QuestionGenerationStrategy strategy = factory.getStrategy(context.effectiveType());
        int count = Math.max(1, context.getQuestionCount());

        try {
            String system = strategy.buildSystemPrompt(context);
            String user = strategy.buildUserPrompt(context);
            String raw = aiService.generateStructuredContent(user, system).block();
            List<GeneratedQuestionDraft> parsed = parseAll(raw, strategy, context);

            List<GeneratedQuestionDraft> validated = new ArrayList<>();
            for (GeneratedQuestionDraft draft : parsed) {
                if (draft == null || draft.getQuestionText() == null || draft.getQuestionText().isBlank()) {
                    continue;
                }
                if (!strategy.isValidCategory(draft.getCategory())
                        && !strategy.supportedType().getCategoryLabel().equalsIgnoreCase(draft.getCategory())) {
                    log.warn("Dropping cross-round question for {}: category={}",
                            strategy.supportedType(), draft.getCategory());
                    continue;
                }
                // Force canonical category label
                draft.setCategory(strategy.supportedType().getCategoryLabel());
                if (draft.getEvaluationCriteria() == null || draft.getEvaluationCriteria().isEmpty()) {
                    draft.setEvaluationCriteria(new ArrayList<>(strategy.defaultEvaluationCriteria()));
                }
                validated.add(draft);
                if (validated.size() >= count) {
                    break;
                }
            }

            if (validated.size() < count) {
                log.warn("AI returned {} valid {} questions (needed {}). Filling from round fallbacks.",
                        validated.size(), strategy.supportedType(), count);
                fillFromFallback(validated, strategy, context, count);
            }

            if (validated.isEmpty()) {
                throw new BusinessException("No valid questions generated for " + strategy.supportedType());
            }
            return validated.subList(0, Math.min(count, validated.size()));
        } catch (Exception e) {
            log.warn("Round-aware AI generation failed for {}: {}. Using typed fallbacks.",
                    context.effectiveType(), e.getMessage());
            List<GeneratedQuestionDraft> fallbacks = new ArrayList<>();
            fillFromFallback(fallbacks, strategy, context, count);
            return fallbacks;
        }
    }

    private void fillFromFallback(
            List<GeneratedQuestionDraft> into,
            QuestionGenerationStrategy strategy,
            QuestionGenerationContext context,
            int count
    ) {
        List<GeneratedQuestionDraft> bank = strategy.fallbackQuestions(context);
        int i = 0;
        while (into.size() < count && !bank.isEmpty()) {
            GeneratedQuestionDraft src = bank.get(i % bank.size());
            GeneratedQuestionDraft copy = GeneratedQuestionDraft.builder()
                    .questionText(src.getQuestionText())
                    .category(strategy.supportedType().getCategoryLabel())
                    .difficultyLevel(src.getDifficultyLevel())
                    .expectedTimeMinutes(src.getExpectedTimeMinutes())
                    .evaluationCriteria(new ArrayList<>(
                            src.getEvaluationCriteria() != null && !src.getEvaluationCriteria().isEmpty()
                                    ? src.getEvaluationCriteria()
                                    : strategy.defaultEvaluationCriteria()))
                    .followUpQuestions(src.getFollowUpQuestions() != null
                            ? new ArrayList<>(src.getFollowUpQuestions())
                            : new ArrayList<>())
                    .hints(src.getHints())
                    .referenceAnswer(src.getReferenceAnswer())
                    .metadata(src.getMetadata() != null ? new java.util.HashMap<>(src.getMetadata()) : new java.util.HashMap<>())
                    .build();
            into.add(copy);
            i++;
            if (i > count * 3) {
                break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<GeneratedQuestionDraft> parseAll(
            String raw,
            QuestionGenerationStrategy strategy,
            QuestionGenerationContext context
    ) throws Exception {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        String json = extractJsonObject(raw);
        Map<String, Object> root = objectMapper.readValue(json, new TypeReference<>() {});
        Object questionsNode = root.get("questions");
        if (!(questionsNode instanceof List<?> list) || list.isEmpty()) {
            return List.of();
        }
        List<GeneratedQuestionDraft> out = new ArrayList<>();
        int order = 1;
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> map)) {
                continue;
            }
            Map<String, Object> q = (Map<String, Object>) map;
            GeneratedQuestionDraft draft = strategy.parseQuestion(q, context, order);
            if (draft == null) {
                // Wrong category — skip (caller may fill fallback). Do not accept cross-round.
                log.warn("Rejected AI question #{} for round {} (invalid/missing fields or wrong category)",
                        order, strategy.supportedType());
            } else {
                out.add(draft);
            }
            order++;
        }
        return out;
    }

    private String extractJsonObject(String response) {
        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');
        if (start < 0 || end < start) {
            throw new BusinessException("AI response did not contain valid JSON");
        }
        return response.substring(start, end + 1);
    }
}
