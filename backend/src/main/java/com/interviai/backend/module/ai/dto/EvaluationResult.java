package com.interviai.backend.module.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Real-time evaluation result after every answer.
 * Covers 7 evaluation dimensions for comprehensive feedback.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationResult {

    /** Overall score 0–100 */
    private Integer score;

    /** Rating: EXCELLENT | GOOD | SATISFACTORY | NEEDS_IMPROVEMENT | POOR */
    private String rating;

    // === 7 Evaluation Dimensions ===
    private Integer technicalAccuracy;   // 0–100
    private Integer communication;       // 0–100
    private Integer confidence;          // 0–100
    private Integer completeness;        // 0–100
    private Integer problemSolving;      // 0–100
    private Integer depth;               // 0–100
    private Integer clarity;             // 0–100

    /** Specific feedback on the answer */
    private String feedback;

    /** What an ideal answer would look like */
    private String expectedAnswer;

    /** Detected weak skills from this answer */
    private List<String> weakSkillsDetected;

    /** Detected strong skills from this answer */
    private List<String> strongSkillsDetected;

    /** Improvement tips (2–3 actionable items) */
    private List<String> improvementTips;

    /** Suggested next difficulty based on this answer */
    private String suggestedNextDifficulty;
}
