package com.interviai.backend.module.interview.generation;

import com.interviai.backend.module.interview.enums.InterviewType;

import java.util.List;
import java.util.Map;

/**
 * Round-specific question generation strategy.
 * Each interview type has its own system prompt, JSON schema, fallbacks, and evaluation criteria.
 */
public interface QuestionGenerationStrategy {

    InterviewType supportedType();

    /** Dedicated system prompt — never shared across rounds. */
    String buildSystemPrompt(QuestionGenerationContext context);

    /** User prompt with role, company, skills, difficulty, etc. */
    String buildUserPrompt(QuestionGenerationContext context);

    /** Type-specific fallback bank when AI fails. Never cross-round. */
    List<GeneratedQuestionDraft> fallbackQuestions(QuestionGenerationContext context);

    /** Default evaluation criteria for this round. */
    List<String> defaultEvaluationCriteria();

    /** True if AI-returned category belongs to this round. */
    boolean isValidCategory(String category);

    /**
     * Parse one AI JSON object into a draft. Returns null if invalid / wrong category.
     */
    GeneratedQuestionDraft parseQuestion(Map<String, Object> raw, QuestionGenerationContext context, int order);
}
