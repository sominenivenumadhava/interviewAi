package com.interviai.backend.module.interview.enums;

public enum DifficultyLevel {
    EASY("Easy", 1),
    MEDIUM("Medium", 2),
    HARD("Hard", 3),
    EXPERT("Expert", 4);
    
    private final String displayName;
    private final int level;
    
    DifficultyLevel(String displayName, int level) {
        this.displayName = displayName;
        this.level = level;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getLevel() {
        return level;
    }
}