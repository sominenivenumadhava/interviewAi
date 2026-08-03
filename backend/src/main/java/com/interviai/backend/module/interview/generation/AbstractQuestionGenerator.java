package com.interviai.backend.module.interview.generation;

import com.interviai.backend.module.interview.enums.DifficultyLevel;
import com.interviai.backend.module.interview.enums.InterviewType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Shared helpers for round-specific generators.
 */
public abstract class AbstractQuestionGenerator implements QuestionGenerationStrategy {

    protected String commonContextBlock(QuestionGenerationContext ctx) {
        return """
            Selected Round: %s
            Job Role: %s
            Company: %s
            Experience / Difficulty Level: %s
            Candidate Skills: %s
            Focus Areas: %s
            Preferred Language: %s
            Job Description: %s
            Resume Summary: %s
            Custom Instructions: %s
            """.formatted(
                ctx.effectiveType().name(),
                ctx.safeRole(),
                ctx.safeCompany(),
                ctx.safeDifficulty(),
                ctx.safeSkills(),
                blank(ctx.getFocusAreas(), "As appropriate for this round"),
                blank(ctx.getPreferredLanguage(), "Not specified"),
                blank(ctx.getJobDescription(), "Not provided"),
                blank(ctx.getResumeContent(), "Not provided"),
                blank(ctx.getCustomInstructions(), "None")
        );
    }

    protected String blank(String value, String fallback) {
        return value != null && !value.isBlank() ? value.trim() : fallback;
    }

    protected String asString(Map<String, Object> raw, String key) {
        Object v = raw.get(key);
        return v == null ? null : String.valueOf(v).trim();
    }

    @SuppressWarnings("unchecked")
    protected List<String> asStringList(Map<String, Object> raw, String key) {
        Object v = raw.get(key);
        if (v instanceof List<?> list) {
            List<String> out = new ArrayList<>();
            for (Object item : list) {
                if (item != null && !String.valueOf(item).isBlank()) {
                    out.add(String.valueOf(item).trim());
                }
            }
            return out;
        }
        return List.of();
    }

    protected DifficultyLevel parseDifficulty(String raw, QuestionGenerationContext ctx) {
        if (raw == null || raw.isBlank()) {
            return ctx.getDifficulty() != null ? ctx.getDifficulty() : DifficultyLevel.MEDIUM;
        }
        try {
            return DifficultyLevel.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            return ctx.getDifficulty() != null ? ctx.getDifficulty() : DifficultyLevel.MEDIUM;
        }
    }

    protected boolean categoryMatches(String category, String... allowed) {
        if (category == null || category.isBlank()) {
            return false;
        }
        String n = category.trim().toLowerCase(Locale.ROOT)
                .replace('_', ' ')
                .replace('-', ' ');
        for (String a : allowed) {
            if (n.equals(a.toLowerCase(Locale.ROOT)) || n.contains(a.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    protected GeneratedQuestionDraft baseDraft(
            String text,
            QuestionGenerationContext ctx,
            List<String> followUps
    ) {
        return GeneratedQuestionDraft.builder()
                .questionText(text)
                .category(supportedType().getCategoryLabel())
                .difficultyLevel(ctx.getDifficulty() != null ? ctx.getDifficulty() : DifficultyLevel.MEDIUM)
                .expectedTimeMinutes(defaultExpectedMinutes())
                .evaluationCriteria(new ArrayList<>(defaultEvaluationCriteria()))
                .followUpQuestions(followUps != null ? new ArrayList<>(followUps) : new ArrayList<>())
                .build();
    }

    protected int defaultExpectedMinutes() {
        return 5;
    }

    @Override
    public GeneratedQuestionDraft parseQuestion(Map<String, Object> raw, QuestionGenerationContext context, int order) {
        String text = asString(raw, "question");
        if (text == null || text.isBlank()) {
            text = asString(raw, "questionText");
        }
        if (text == null || text.isBlank()) {
            return null;
        }
        String category = asString(raw, "category");
        if (!isValidCategory(category) && !isValidCategory(supportedType().getCategoryLabel())) {
            // Allow missing category only if we force-label below; reject wrong categories
            if (category != null && !category.isBlank() && !isValidCategory(category)) {
                return null;
            }
        }
        if (category != null && !category.isBlank() && !isValidCategory(category)) {
            return null;
        }

        List<String> criteria = asStringList(raw, "evaluationCriteria");
        if (criteria.isEmpty()) {
            criteria = new ArrayList<>(defaultEvaluationCriteria());
        }
        List<String> followUps = asStringList(raw, "followUpQuestions");
        Integer minutes = null;
        Object et = raw.get("expectedTimeMinutes");
        if (et instanceof Number n) {
            minutes = n.intValue();
        }

        return GeneratedQuestionDraft.builder()
                .questionText(text)
                .category(supportedType().getCategoryLabel())
                .difficultyLevel(parseDifficulty(asString(raw, "difficulty"), context))
                .expectedTimeMinutes(minutes != null ? minutes : defaultExpectedMinutes())
                .evaluationCriteria(criteria)
                .followUpQuestions(followUps)
                .hints(asString(raw, "hints"))
                .referenceAnswer(asString(raw, "referenceAnswer"))
                .build();
    }
}
