package com.interviai.backend.module.interview.dto.response;

import java.util.UUID;
import com.interviai.backend.module.interview.enums.DifficultyLevel;
import lombok.Data;

import java.util.List;

@Data
public class InterviewQuestionResponse {
    private UUID id;
    private Integer questionOrder;
    private String questionText;
    private String category;
    private DifficultyLevel difficultyLevel;
    private Integer expectedTimeMinutes;
    private List<String> evaluationCriteria;
    private List<String> followUpQuestions;
    private String hints;
    private Boolean isAnswered;
    private InterviewAnswerResponse answer;
}
