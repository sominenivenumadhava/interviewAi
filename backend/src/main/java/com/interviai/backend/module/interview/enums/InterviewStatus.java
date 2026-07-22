package com.interviai.backend.module.interview.enums;

public enum InterviewStatus {
    SCHEDULED("Scheduled"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    PAUSED("Paused"),
    EXPIRED("Expired");
    
    private final String displayName;
    
    InterviewStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}