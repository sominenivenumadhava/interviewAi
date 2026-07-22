package com.interviai.backend.module.resume.entity;

import com.interviai.backend.common.entity.AuditableEntity;
import com.interviai.backend.module.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Resume entity representing user's resume information.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Entity
@Table(name = "resumes", indexes = {
    @Index(name = "idx_resumes_user_id", columnList = "user_id"),
    @Index(name = "idx_resumes_created_at", columnList = "created_at"),
    @Index(name = "idx_resumes_status", columnList = "status"),
    @Index(name = "idx_resumes_is_primary", columnList = "is_primary")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Resume extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_resumes_user"))
    private User user;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;

    @Column(name = "parsed_content", columnDefinition = "JSONB")
    private String parsedContent;

    @Column(name = "full_name", length = 200)
    private String fullName;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "total_experience_years")
    private Integer totalExperienceYears;

    @Column(name = "total_experience_months")
    private Integer totalExperienceMonths;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ResumeStatus status = ResumeStatus.PROCESSING;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;

    @Column(name = "processing_started_at")
    private LocalDateTime processingStartedAt;

    @Column(name = "processing_completed_at")
    private LocalDateTime processingCompletedAt;

    @Column(name = "processing_error", columnDefinition = "TEXT")
    private String processingError;

    @Column(name = "parsing_confidence_score")
    private Double parsingConfidenceScore;

    // Relationships
    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Education> educations = new ArrayList<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<WorkExperience> workExperiences = new ArrayList<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Skill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Project> projects = new ArrayList<>();

    // Business Methods

    /**
     * Check if resume processing is completed successfully.
     */
    public boolean isProcessingCompleted() {
        return status == ResumeStatus.COMPLETED;
    }

    /**
     * Check if resume processing failed.
     */
    public boolean isProcessingFailed() {
        return status == ResumeStatus.FAILED;
    }

    /**
     * Check if resume is currently being processed.
     */
    public boolean isProcessing() {
        return status == ResumeStatus.PROCESSING;
    }

    /**
     * Mark resume processing as started.
     */
    public void startProcessing() {
        this.status = ResumeStatus.PROCESSING;
        this.processingStartedAt = LocalDateTime.now();
        this.processingError = null;
    }

    /**
     * Mark resume processing as completed successfully.
     */
    public void completeProcessing(double confidenceScore) {
        this.status = ResumeStatus.COMPLETED;
        this.processingCompletedAt = LocalDateTime.now();
        this.parsingConfidenceScore = confidenceScore;
        this.processingError = null;
    }

    /**
     * Mark resume processing as failed.
     */
    public void failProcessing(String error) {
        this.status = ResumeStatus.FAILED;
        this.processingCompletedAt = LocalDateTime.now();
        this.processingError = error;
    }

    /**
     * Set this resume as primary and unset others.
     */
    public void setPrimary() {
        this.isPrimary = true;
    }

    /**
     * Unset this resume as primary.
     */
    public void unsetPrimary() {
        this.isPrimary = false;
    }

    /**
     * Add education to resume.
     */
    public void addEducation(Education education) {
        educations.add(education);
        education.setResume(this);
    }

    /**
     * Remove education from resume.
     */
    public void removeEducation(Education education) {
        educations.remove(education);
        education.setResume(null);
    }

    /**
     * Add work experience to resume.
     */
    public void addWorkExperience(WorkExperience workExperience) {
        workExperiences.add(workExperience);
        workExperience.setResume(this);
    }

    /**
     * Remove work experience from resume.
     */
    public void removeWorkExperience(WorkExperience workExperience) {
        workExperiences.remove(workExperience);
        workExperience.setResume(null);
    }

    /**
     * Add skill to resume.
     */
    public void addSkill(Skill skill) {
        skills.add(skill);
        skill.setResume(this);
    }

    /**
     * Remove skill from resume.
     */
    public void removeSkill(Skill skill) {
        skills.remove(skill);
        skill.setResume(null);
    }
    
    /**
     * Find skill by name.
     */
    public Skill getSkillByName(String skillName) {
        if (skillName == null || skills == null) {
            return null;
        }
        return skills.stream()
                .filter(skill -> skill.getName() != null && skill.getName().equalsIgnoreCase(skillName))
                .findFirst()
                .orElse(null);
    }

    /**
     * Add project to resume.
     */
    public void addProject(Project project) {
        projects.add(project);
        project.setResume(this);
    }

    /**
     * Remove project from resume.
     */
    public void removeProject(Project project) {
        projects.remove(project);
        project.setResume(null);
    }

    /**
     * Calculate total experience in months.
     */
    public int getTotalExperienceInMonths() {
        if (totalExperienceYears == null && totalExperienceMonths == null) {
            return 0;
        }
        int years = totalExperienceYears != null ? totalExperienceYears : 0;
        int months = totalExperienceMonths != null ? totalExperienceMonths : 0;
        return (years * 12) + months;
    }

    /**
     * Get formatted experience string.
     */
    public String getFormattedExperience() {
        int years = totalExperienceYears != null ? totalExperienceYears : 0;
        int months = totalExperienceMonths != null ? totalExperienceMonths : 0;
        
        if (years == 0 && months == 0) {
            return "No experience";
        }
        
        StringBuilder sb = new StringBuilder();
        if (years > 0) {
            sb.append(years).append(years == 1 ? " year" : " years");
        }
        if (months > 0) {
            if (years > 0) {
                sb.append(" ");
            }
            sb.append(months).append(months == 1 ? " month" : " months");
        }
        
        return sb.toString();
    }

    /**
     * Get file size in human readable format.
     */
    public String getFormattedFileSize() {
        if (fileSize == null) {
            return "Unknown";
        }
        
        long bytes = fileSize;
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }

    /**
     * Resume processing status enum.
     */
    public enum ResumeStatus {
        UPLOADED("Uploaded"),
        PROCESSING("Processing"),
        COMPLETED("Completed"),
        FAILED("Failed");

        private final String displayName;

        ResumeStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}