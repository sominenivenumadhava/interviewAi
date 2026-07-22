package com.interviai.backend.module.interview.entity;

import com.interviai.backend.common.entity.BaseEntity;
import com.interviai.backend.module.interview.enums.DifficultyLevel;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "interview_questions")
@Data
@EqualsAndHashCode(callSuper = true)
public class InterviewQuestion extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;
    
    @Column(name = "question_order", nullable = false)
    private Integer questionOrder;
    
    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    private String questionText;
    
    @Column(name = "category")
    private String category;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level")
    private DifficultyLevel difficultyLevel;
    
    @Column(name = "expected_time_minutes")
    private Integer expectedTimeMinutes;
    
    @ElementCollection
    @CollectionTable(name = "question_evaluation_criteria", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "criteria")
    private List<String> evaluationCriteria = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "question_follow_ups", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "follow_up")
    private List<String> followUpQuestions = new ArrayList<>();
    
    @Column(name = "ai_generated", nullable = false)
    private Boolean aiGenerated = true;
    
    @Column(name = "reference_answer", columnDefinition = "TEXT")
    private String referenceAnswer;
    
    @Column(name = "hints", columnDefinition = "TEXT")
    private String hints;
    
    @OneToOne(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private InterviewAnswer answer;
    
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;
    
    // Utility methods
    public boolean isAnswered() {
        return answer != null;
    }
    
    public void setAnswer(InterviewAnswer answer) {
        this.answer = answer;
        if (answer != null) {
            answer.setQuestion(this);
        }
    }
}