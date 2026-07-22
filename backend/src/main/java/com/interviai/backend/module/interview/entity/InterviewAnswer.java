package com.interviai.backend.module.interview.entity;

import com.interviai.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "interview_answers")
@Data
@EqualsAndHashCode(callSuper = true)
public class InterviewAnswer extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private InterviewQuestion question;
    
    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;
    
    @Column(name = "answer_audio_url")
    private String answerAudioUrl;
    
    @Column(name = "answer_video_url")
    private String answerVideoUrl;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;
    
    @Column(name = "time_taken_seconds")
    private Long timeTakenSeconds;
    
    public java.time.Duration getTimeTaken() {
        return timeTakenSeconds != null ? java.time.Duration.ofSeconds(timeTakenSeconds) : null;
    }
    
    public void setTimeTaken(java.time.Duration duration) {
        this.timeTakenSeconds = duration != null ? duration.toSeconds() : null;
    }
    
    @Column(name = "score")
    private Double score;
    
    @Column(name = "rating")
    private String rating;
    
    @ElementCollection
    @CollectionTable(name = "answer_strengths", joinColumns = @JoinColumn(name = "answer_id"))
    @Column(name = "strength")
    private List<String> strengths = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "answer_improvements", joinColumns = @JoinColumn(name = "answer_id"))
    @Column(name = "improvement")
    private List<String> improvements = new ArrayList<>();
    
    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;
    
    @Column(name = "suggested_answer", columnDefinition = "TEXT")
    private String suggestedAnswer;
    
    @Column(name = "ai_evaluation", columnDefinition = "jsonb")
    private String aiEvaluation;
    
    @Column(name = "confidence_score")
    private Double confidenceScore;
    
    @Column(name = "clarity_score")
    private Double clarityScore;
    
    @Column(name = "relevance_score")
    private Double relevanceScore;
    
    @Column(name = "technical_accuracy_score")
    private Double technicalAccuracyScore;
    
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;
    
    // Utility methods
    public void startAnswering() {
        this.startedAt = LocalDateTime.now();
    }
    
    public void submit() {
        this.submittedAt = LocalDateTime.now();
        if (this.startedAt != null) {
            this.timeTakenSeconds = java.time.Duration.between(this.startedAt, this.submittedAt).toSeconds();
        }
    }
    
    public boolean isSubmitted() {
        return submittedAt != null;
    }
    
    public boolean isEvaluated() {
        return score != null && feedback != null;
    }
}