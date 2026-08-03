package com.interviai.backend.module.interview.generation;

import com.interviai.backend.module.interview.enums.DifficultyLevel;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Normalized draft produced by a round-specific generator (AI or fallback).
 */
@Data
@Builder
public class GeneratedQuestionDraft {
    private String questionText;
    private String category;
    private DifficultyLevel difficultyLevel;
    private Integer expectedTimeMinutes;
    @Builder.Default
    private List<String> evaluationCriteria = new ArrayList<>();
    @Builder.Default
    private List<String> followUpQuestions = new ArrayList<>();
    private String hints;
    private String referenceAnswer;
    /** Coding / structured extras: constraints, samples, complexity, topic, etc. */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
}
