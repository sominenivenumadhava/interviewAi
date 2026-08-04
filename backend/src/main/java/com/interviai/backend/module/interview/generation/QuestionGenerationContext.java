package com.interviai.backend.module.interview.generation;

import com.interviai.backend.module.interview.enums.DifficultyLevel;
import com.interviai.backend.module.interview.enums.InterviewType;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Context passed into every round-specific question generator.
 * Includes session uniqueness fields so each interview produces a fresh set.
 */
@Data
@Builder
public class QuestionGenerationContext {
    private InterviewType interviewType;
    private String role;
    private String company;
    private String jobDescription;
    private DifficultyLevel difficulty;
    private int questionCount;
    private String focusAreas;
    private String customInstructions;
    private String resumeContent;
    private String candidateSkills;
    private String preferredLanguage;
    private String experienceLevel;

    /** Unique interview session id — used as diversity seed. */
    private String sessionId;

    /** Stable numeric seed derived from sessionId (+ nonce). */
    private long sessionSeed;

    /** Extra entropy so regenerations differ even for the same session. */
    private String diversityNonce;

    /** Texts already asked in this interview (avoid exact/near duplicates). */
    @Builder.Default
    private List<String> askedQuestions = new ArrayList<>();

    /** Topics/concepts already used. */
    @Builder.Default
    private List<String> askedTopics = new ArrayList<>();

    public InterviewType effectiveType() {
        return interviewType != null ? interviewType.canonicalize() : InterviewType.TECHNICAL;
    }

    public String safeRole() {
        return blankTo(role, "Software Engineer");
    }

    public String safeCompany() {
        return blankTo(company, "the company");
    }

    public String safeDifficulty() {
        return difficulty != null ? difficulty.name() : "MEDIUM";
    }

    public String safeSkills() {
        return blankTo(candidateSkills, "General software engineering skills");
    }

    public String safeExperience() {
        return blankTo(experienceLevel, "Mid-level");
    }

    public String safeSessionId() {
        return blankTo(sessionId, UUID.randomUUID().toString());
    }

    public String safeDiversityNonce() {
        return blankTo(diversityNonce, Long.toHexString(System.nanoTime()));
    }

    private static String blankTo(String value, String fallback) {
        return value != null && !value.isBlank() ? value.trim() : fallback;
    }
}
