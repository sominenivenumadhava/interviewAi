package com.interviai.backend.module.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result from AI generating the next interview question.
 * Includes the question text, suggested difficulty adjustment, and interview flow guidance.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NextQuestionResult {

    /** The AI-generated question text */
    private String question;

    /** Brief greeting or transition phrase (e.g., "Great answer! Now let's talk about...") */
    private String transitionPhrase;

    /** Question category: technical | behavioral | system_design | hr | coding */
    private String category;

    /** Difficulty of this specific question */
    private String difficulty;

    /** Expected time in minutes */
    private Integer expectedMinutes;

    /** Evaluation criteria hints for scoring */
    private java.util.List<String> evaluationCriteria;

    /** Interview phase this question belongs to */
    private String interviewPhase;

    /** If true, this is the last question (interviewer should move to closing) */
    private boolean isLastQuestion;

    /** Suggested next difficulty based on performance */
    private String suggestedNextDifficulty;
}
