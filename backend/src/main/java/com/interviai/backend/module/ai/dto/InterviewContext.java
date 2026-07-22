package com.interviai.backend.module.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Complete interview context passed to the AI engine for every request.
 * Contains everything needed to generate a perfect, contextual question.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewContext {

    // Session identifiers
    private String sessionId;

    // Candidate profile
    private String candidateName;
    private String resumeText;       // full resume content for AI to read
    private List<String> skills;
    private List<String> projects;
    private String experience;       // e.g., "3 years"

    // Interview config
    private String company;          // e.g., "Google"
    private String role;             // e.g., "Senior Frontend Engineer"
    private String interviewType;    // TECHNICAL | BEHAVIORAL | HR | SYSTEM_DESIGN | CODING | MIXED
    private String difficulty;       // EASY | MEDIUM | HARD
    private Integer durationMinutes;
    private Integer totalQuestions;
    private Integer currentQuestionNumber;  // 1-indexed
    private String interviewPhase;          // GREETING | WARMUP | CORE | DEEP_DIVE | BEHAVIORAL | CLOSING

    // Conversation history (for follow-up intelligence)
    private List<ConversationTurn> conversationHistory;

    // Adaptive difficulty state
    private Double currentScore;          // rolling average 0-100
    private String currentDifficulty;     // tracks real-time difficulty (may differ from initial)
    private List<String> weakSkills;      // detected weak areas
    private List<String> strongSkills;    // detected strong areas

    // Focus areas (user-selected or AI-detected)
    private String focusAreas;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConversationTurn {
        private String role;    // "interviewer" or "candidate"
        private String content;
        private Double score;   // null for interviewer turns
    }
}
