package com.interviai.backend.module.interview.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Interview round types. Each maps to a dedicated {@code QuestionGenerationStrategy}.
 * Legacy {@code BEHAVIORAL} / {@code MIXED} are accepted for old sessions and remapped.
 */
public enum InterviewType {
    HR("HR Round"),
    TECHNICAL("Technical Round"),
    CODING("Coding Round"),
    SYSTEM_DESIGN("System Design Round"),
    MANAGERIAL("Managerial / Bar Raiser Round"),
    APTITUDE("Aptitude / Assessment Round"),
    /** @deprecated Remapped to {@link #HR} for question generation. */
    BEHAVIORAL("Behavioral Interview"),
    /** @deprecated Remapped to {@link #TECHNICAL} for question generation. */
    MIXED("Mixed Interview");

    private final String displayName;

    InterviewType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCategoryLabel() {
        return switch (canonicalize()) {
            case HR -> "HR";
            case TECHNICAL -> "Technical";
            case CODING -> "Coding";
            case SYSTEM_DESIGN -> "System Design";
            case MANAGERIAL -> "Managerial";
            case APTITUDE -> "Aptitude";
            default -> name();
        };
    }

    /** Maps legacy types onto the six canonical generators. */
    public InterviewType canonicalize() {
        return switch (this) {
            case BEHAVIORAL -> HR;
            case MIXED -> TECHNICAL;
            default -> this;
        };
    }

    @JsonValue
    public String toJson() {
        return name();
    }

    @JsonCreator
    public static InterviewType fromJson(String value) {
        if (value == null || value.isBlank()) {
            return TECHNICAL;
        }
        String normalized = value.trim().toUpperCase().replace('-', '_').replace(' ', '_');
        return switch (normalized) {
            case "BEHAVIORAL", "BEHAVIOURAL" -> BEHAVIORAL;
            case "HR", "HR_SCREEN", "SCREEN" -> HR;
            case "TECHNICAL", "TECH" -> TECHNICAL;
            case "CODING", "OA", "DSA" -> CODING;
            case "SYSTEM_DESIGN", "SYSTEMDESIGN", "LLD", "HLD" -> SYSTEM_DESIGN;
            case "MANAGERIAL", "BAR_RAISER", "BARRAISER", "LEADERSHIP" -> MANAGERIAL;
            case "APTITUDE", "ASSESSMENT" -> APTITUDE;
            case "MIXED", "FULL_LOOP" -> MIXED;
            default -> InterviewType.valueOf(normalized);
        };
    }
}
