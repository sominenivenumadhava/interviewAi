package com.interviai.backend.module.interview.enums;

public enum InterviewType {
    TECHNICAL("Technical Interview"),
    BEHAVIORAL("Behavioral Interview"),
    CODING("Coding Interview"),
    SYSTEM_DESIGN("System Design Interview"),
    MIXED("Mixed Interview");
    
    private final String displayName;
    
    InterviewType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}