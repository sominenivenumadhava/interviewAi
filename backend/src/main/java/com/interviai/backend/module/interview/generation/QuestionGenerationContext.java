package com.interviai.backend.module.interview.generation;

import com.interviai.backend.module.interview.enums.DifficultyLevel;
import com.interviai.backend.module.interview.enums.InterviewType;
import lombok.Builder;
import lombok.Data;

/**
 * Context passed into every round-specific question generator.
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

    private static String blankTo(String value, String fallback) {
        return value != null && !value.isBlank() ? value.trim() : fallback;
    }
}
