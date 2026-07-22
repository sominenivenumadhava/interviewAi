package com.interviai.backend.module.resume.entity;

import com.interviai.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Education entity representing educational qualifications in resume.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Entity
@Table(name = "resume_educations", indexes = {
    @Index(name = "idx_resume_educations_resume_id", columnList = "resume_id"),
    @Index(name = "idx_resume_educations_degree_type", columnList = "degree_type"),
    @Index(name = "idx_resume_educations_end_date", columnList = "end_date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Education extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false, foreignKey = @ForeignKey(name = "fk_resume_educations_resume"))
    private Resume resume;

    @Column(name = "institution_name", nullable = false, length = 200)
    private String institutionName;

    @Column(name = "degree", nullable = false, length = 100)
    private String degree;

    @Column(name = "field_of_study", length = 100)
    private String fieldOfStudy;

    @Enumerated(EnumType.STRING)
    @Column(name = "degree_type", nullable = false, length = 20)
    private DegreeType degreeType;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_current", nullable = false)
    @Builder.Default
    private Boolean isCurrent = false;

    @Column(name = "gpa")
    private Double gpa;

    @Column(name = "max_gpa")
    private Double maxGpa;

    @Column(name = "percentage")
    private Double percentage;

    @Column(name = "grade", length = 10)
    private String grade;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "honors", length = 200)
    private String honors;

    @Column(name = "relevant_coursework", columnDefinition = "TEXT")
    private String relevantCoursework;

    @Column(name = "activities", columnDefinition = "TEXT")
    private String activities;

    @Column(name = "display_order")
    private Integer displayOrder;

    // Business Methods

    /**
     * Check if education is currently ongoing.
     */
    public boolean isCurrentlyStudying() {
        return Boolean.TRUE.equals(isCurrent);
    }

    /**
     * Get duration of education in years.
     */
    public Double getDurationInYears() {
        if (startDate == null) {
            return null;
        }
        
        LocalDate endDateToUse = isCurrent ? LocalDate.now() : endDate;
        if (endDateToUse == null) {
            return null;
        }
        
        return (double) java.time.Period.between(startDate, endDateToUse).toTotalMonths() / 12.0;
    }

    /**
     * Get formatted GPA string.
     */
    public String getFormattedGpa() {
        if (gpa == null) {
            return null;
        }
        
        if (maxGpa != null) {
            return String.format("%.2f/%.2f", gpa, maxGpa);
        } else {
            return String.format("%.2f", gpa);
        }
    }

    /**
     * Get formatted date range.
     */
    public String getFormattedDateRange() {
        StringBuilder sb = new StringBuilder();
        
        if (startDate != null) {
            sb.append(startDate.getMonthValue()).append("/").append(startDate.getYear());
        } else {
            sb.append("Unknown");
        }
        
        sb.append(" - ");
        
        if (isCurrent) {
            sb.append("Present");
        } else if (endDate != null) {
            sb.append(endDate.getMonthValue()).append("/").append(endDate.getYear());
        } else {
            sb.append("Unknown");
        }
        
        return sb.toString();
    }

    /**
     * Get full degree name with field of study.
     */
    public String getFullDegreeName() {
        StringBuilder sb = new StringBuilder(degree);
        
        if (fieldOfStudy != null && !fieldOfStudy.trim().isEmpty()) {
            sb.append(" in ").append(fieldOfStudy);
        }
        
        return sb.toString();
    }

    /**
     * Check if education has performance metrics.
     */
    public boolean hasPerformanceMetrics() {
        return gpa != null || percentage != null || grade != null;
    }

    /**
     * Degree type enum.
     */
    public enum DegreeType {
        HIGH_SCHOOL("High School"),
        DIPLOMA("Diploma"),
        ASSOCIATE("Associate Degree"),
        BACHELOR("Bachelor's Degree"),
        MASTER("Master's Degree"),
        DOCTORATE("Doctorate"),
        CERTIFICATE("Certificate"),
        PROFESSIONAL("Professional Degree"),
        OTHER("Other");

        private final String displayName;

        DegreeType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static DegreeType fromString(String value) {
            if (value == null) {
                return OTHER;
            }
            
            String normalized = value.toLowerCase().trim();
            
            if (normalized.contains("high school") || normalized.contains("secondary")) {
                return HIGH_SCHOOL;
            } else if (normalized.contains("diploma")) {
                return DIPLOMA;
            } else if (normalized.contains("associate")) {
                return ASSOCIATE;
            } else if (normalized.contains("bachelor") || normalized.contains("b.")) {
                return BACHELOR;
            } else if (normalized.contains("master") || normalized.contains("m.")) {
                return MASTER;
            } else if (normalized.contains("phd") || normalized.contains("doctorate") || normalized.contains("ph.d")) {
                return DOCTORATE;
            } else if (normalized.contains("certificate") || normalized.contains("cert")) {
                return CERTIFICATE;
            } else if (normalized.contains("professional")) {
                return PROFESSIONAL;
            }
            
            return OTHER;
        }
    }
}