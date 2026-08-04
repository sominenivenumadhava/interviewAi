package com.interviai.backend.module.interview.generation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviai.backend.common.exception.BusinessException;
import com.interviai.backend.module.ai.service.AIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Orchestrates round-specific AI generation with category validation,
 * creative temperature, session-seeded fallback shuffle, and anti-repeat.
 */
@Service
public class RoundAwareQuestionGenerator {

    private static final Logger log = LoggerFactory.getLogger(RoundAwareQuestionGenerator.class);
    private static final double SIMILARITY_THRESHOLD = 0.72;

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
        ensureSessionSeed(context);

        try {
            String system = strategy.buildSystemPrompt(context);
            String user = strategy.buildUserPrompt(context);
            String raw = aiService.generateCreativeStructuredContent(user, system).block();
            List<GeneratedQuestionDraft> parsed = parseAll(raw, strategy, context);

            List<GeneratedQuestionDraft> validated = new ArrayList<>();
            List<String> accepted = new ArrayList<>(
                    context.getAskedQuestions() != null ? context.getAskedQuestions() : List.of());

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
                if (QuestionUniquenessHelper.isTooSimilar(draft.getQuestionText(), accepted, SIMILARITY_THRESHOLD)) {
                    log.debug("Skipping AI question similar to prior/asked set");
                    continue;
                }
                draft.setCategory(strategy.supportedType().getCategoryLabel());
                if (draft.getEvaluationCriteria() == null || draft.getEvaluationCriteria().isEmpty()) {
                    draft.setEvaluationCriteria(new ArrayList<>(strategy.defaultEvaluationCriteria()));
                }
                // Randomize follow-up wording diversity marker
                diversifyFollowUps(draft, context.getSessionSeed() + validated.size());
                validated.add(draft);
                accepted.add(draft.getQuestionText());
                if (validated.size() >= count) {
                    break;
                }
            }

            if (validated.size() < count) {
                log.warn("AI returned {} valid {} questions (needed {}). Filling from shuffled round fallbacks.",
                        validated.size(), strategy.supportedType(), count);
                fillFromFallback(validated, strategy, context, count, accepted);
            }

            if (validated.isEmpty()) {
                throw new BusinessException("No valid questions generated for " + strategy.supportedType());
            }
            return validated.subList(0, Math.min(count, validated.size()));
        } catch (Exception e) {
            log.warn("Round-aware AI generation failed for {}: {}. Using shuffled typed fallbacks.",
                    context.effectiveType(), e.getMessage());
            List<GeneratedQuestionDraft> fallbacks = new ArrayList<>();
            List<String> accepted = new ArrayList<>(
                    context.getAskedQuestions() != null ? context.getAskedQuestions() : List.of());
            fillFromFallback(fallbacks, strategy, context, count, accepted);
            return fallbacks;
        }
    }

    private void ensureSessionSeed(QuestionGenerationContext context) {
        if (context.getSessionId() == null || context.getSessionId().isBlank()) {
            context.setSessionId(java.util.UUID.randomUUID().toString());
        }
        if (context.getDiversityNonce() == null || context.getDiversityNonce().isBlank()) {
            context.setDiversityNonce(java.util.UUID.randomUUID().toString());
        }
        if (context.getSessionSeed() == 0L) {
            context.setSessionSeed(QuestionUniquenessHelper.seedFrom(
                    context.getSessionId(), context.getDiversityNonce()));
        }
    }

    private void diversifyFollowUps(GeneratedQuestionDraft draft, long seed) {
        if (draft.getFollowUpQuestions() == null || draft.getFollowUpQuestions().isEmpty()) {
            String[] pool = {
                    "Can this approach be optimized further?",
                    "What happens for edge cases or empty input?",
                    "How would you solve this with limited memory?",
                    "What if the input size becomes 10^7?",
                    "Can this be parallelized or made asynchronous?",
                    "What trade-offs did you make, and why?",
                    "How would you test this thoroughly?",
                    "What would you do differently with more time?"
            };
            int idx = (int) Math.floorMod(seed, pool.length);
            draft.setFollowUpQuestions(new ArrayList<>(List.of(pool[idx], pool[(idx + 3) % pool.length])));
        } else {
            draft.setFollowUpQuestions(QuestionUniquenessHelper.shuffledCopy(
                    draft.getFollowUpQuestions(), seed));
        }
    }

    private void fillFromFallback(
            List<GeneratedQuestionDraft> into,
            QuestionGenerationStrategy strategy,
            QuestionGenerationContext context,
            int count,
            List<String> accepted
    ) {
        List<GeneratedQuestionDraft> bank = strategy.fallbackQuestions(context);
        // Session-seeded shuffle so different interviews never start at bank[0]
        long seed = context.getSessionSeed() != 0L
                ? context.getSessionSeed()
                : QuestionUniquenessHelper.seedFrom(context.safeSessionId(), context.safeDiversityNonce());
        bank = QuestionUniquenessHelper.shuffledCopy(bank, seed ^ (into.size() * 31L + 17L));

        int i = 0;
        int attempts = 0;
        while (into.size() < count && !bank.isEmpty() && attempts < bank.size() * 4) {
            GeneratedQuestionDraft src = bank.get(i % bank.size());
            i++;
            attempts++;
            if (src == null || src.getQuestionText() == null || src.getQuestionText().isBlank()) {
                continue;
            }
            if (QuestionUniquenessHelper.isTooSimilar(src.getQuestionText(), accepted, SIMILARITY_THRESHOLD)) {
                continue;
            }
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
                            ? QuestionUniquenessHelper.shuffledCopy(src.getFollowUpQuestions(), seed + i)
                            : new ArrayList<>())
                    .hints(src.getHints())
                    .referenceAnswer(src.getReferenceAnswer())
                    .metadata(src.getMetadata() != null ? new HashMap<>(src.getMetadata()) : new HashMap<>())
                    .build();
            diversifyFollowUps(copy, seed + into.size());
            into.add(copy);
            accepted.add(copy.getQuestionText());
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
                log.warn("Rejected AI question #{} for round {} (invalid/missing fields or wrong category)",
                        order, strategy.supportedType());
            } else {
                out.add(draft);
            }
            order++;
        }
        // Shuffle AI order with session seed so Q1 is not always the model's first pick
        return QuestionUniquenessHelper.shuffledCopy(out, context.getSessionSeed());
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
