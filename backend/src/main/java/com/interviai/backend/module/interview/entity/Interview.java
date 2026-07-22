package com.interviai.backend.module.interview.entity;

import com.interviai.backend.common.entity.AuditableEntity;
import com.interviai.backend.module.interview.enums.DifficultyLevel;
import com.interviai.backend.module.interview.enums.InterviewStatus;
import com.interviai.backend.module.interview.enums.InterviewType;
import com.interviai.backend.module.user.entity.User;
import com.interviai.backend.module.resume.entity.Resume;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "interviews")
@Data
@EqualsAndHashCode(callSuper = true)
public class Interview extends AuditableEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    private Resume resume;
    
    @Column(name = "session_id", unique = true, nullable = false)
    private String sessionId;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "company")
    private String company;
    
    @Column(name = "role", nullable = false)
    private String role;
    
    @Column(name = "job_description", columnDefinition = "TEXT")
    private String jobDescription;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "interview_type", nullable = false)
    private InterviewType interviewType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", nullable = false)
    private DifficultyLevel difficultyLevel;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InterviewStatus status;
    
    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "duration_minutes")
    private Integer durationMinutes;
    
    @Column(name = "actual_duration_seconds")
    private Long actualDurationSeconds;
    
    public java.time.Duration getActualDuration() {
        return actualDurationSeconds != null ? java.time.Duration.ofSeconds(actualDurationSeconds) : null;
    }
    
    public void setActualDuration(java.time.Duration duration) {
        this.actualDurationSeconds = duration != null ? duration.toSeconds() : null;
    }
    
    @Column(name = "overall_score")
    private Double overallScore;
    
    @Column(name = "overall_feedback", columnDefinition = "TEXT")
    private String overallFeedback;
    
    @Column(name = "strengths", columnDefinition = "TEXT")
    private String strengths;
    
    @Column(name = "improvements", columnDefinition = "TEXT")
    private String improvements;
    
    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InterviewQuestion> questions = new ArrayList<>();
    
    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InterviewAnswer> answers = new ArrayList<>();
    
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "configuration", columnDefinition = "jsonb")
    private String configuration;
    
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;
    
    // Utility methods
    public void addQuestion(InterviewQuestion question) {
        questions.add(question);
        question.setInterview(this);
    }
    
    public void removeQuestion(InterviewQuestion question) {
        questions.remove(question);
        question.setInterview(null);
    }
    
    public void addAnswer(InterviewAnswer answer) {
        answers.add(answer);
        answer.setInterview(this);
    }
    
    public void removeAnswer(InterviewAnswer answer) {
        answers.remove(answer);
        answer.setInterview(null);
    }
    
    public boolean isActive() {
        return status == InterviewStatus.IN_PROGRESS || status == InterviewStatus.PAUSED;
    }
    
    public boolean isCompleted() {
        return status == InterviewStatus.COMPLETED;
    }
    
    public void start() {
        this.status = InterviewStatus.IN_PROGRESS;
        this.startedAt = LocalDateTime.now();
    }
    
    public void complete() {
        this.status = InterviewStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        if (this.startedAt != null) {
            this.actualDurationSeconds = java.time.Duration.between(this.startedAt, this.completedAt).toSeconds();
        }
    }
    
    public void pause() {
        if (this.status == InterviewStatus.IN_PROGRESS) {
            this.status = InterviewStatus.PAUSED;
        }
    }
    
    public void resume() {
        if (this.status == InterviewStatus.PAUSED) {
            this.status = InterviewStatus.IN_PROGRESS;
        }
    }
    
    public void cancel() {
        this.status = InterviewStatus.CANCELLED;
    }
}