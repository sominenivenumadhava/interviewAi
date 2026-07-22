package com.interviai.backend.module.resume.entity;

import com.interviai.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.Period;

/**
 * Work Experience entity representing professional experience in resume.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Entity
@Table(name = "resume_work_experiences", indexes = {
    @Index(name = "idx_resume_work_experiences_resume_id", columnList = "resume_id"),
    @Index(name = "idx_resume_work_experiences_company", columnList = "company_name"),
    @Index(name = "idx_resume_work_experiences_end_date", columnList = "end_date"),
    @Index(name = "idx_resume_work_experiences_is_current", columnList = "is_current")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WorkExperience extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false, foreignKey = @ForeignKey(name = "fk_resume_work_experiences_resume"))
    private Resume resume;

    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    @Column(name = "job_title", nullable = false, length = 100)
    private String jobTitle;

    @Column(name = "employment_type", length = 50)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EmploymentType employmentType = EmploymentType.FULL_TIME;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_current", nullable = false)
    @Builder.Default
    private Boolean isCurrent = false;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "key_achievements", columnDefinition = "TEXT")
    private String keyAchievements;

    @Column(name = "technologies_used", columnDefinition = "TEXT")
    private String technologiesUsed;

    @Column(name = "industry", length = 100)
    private String industry;

    @Column(name = "company_size", length = 50)
    private String companySize;

    @Column(name = "reporting_to", length = 100)
    private String reportingTo;

    @Column(name = "team_size")
    private Integer teamSize;

    @Column(name = "salary_range", length = 50)
    private String salaryRange;

    @Column(name = "display_order")
    private Integer displayOrder;

    // Business Methods

    /**
     * Check if this is current job.
     */
    public boolean isCurrentJob() {
        return Boolean.TRUE.equals(isCurrent);
    }

    /**
     * Get duration of work experience in months.
     */
    public int getDurationInMonths() {
        if (startDate == null) {
            return 0;
        }
        
        LocalDate endDateToUse = isCurrent ? LocalDate.now() : endDate;
        if (endDateToUse == null) {
            return 0;
        }
        
        return (int) Period.between(startDate, endDateToUse).toTotalMonths();
    }

    /**
     * Get duration of work experience in years and months.
     */
    public String getFormattedDuration() {
        int totalMonths = getDurationInMonths();
        
        if (totalMonths == 0) {
            return "Less than 1 month";
        }
        
        int years = totalMonths / 12;
        int months = totalMonths % 12;
        
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
     * Get full position title with company.
     */
    public String getFullPositionTitle() {
        return jobTitle + " at " + companyName;
    }

    /**
     * Check if experience has detailed information.
     */
    public boolean hasDetailedInfo() {
        return description != null && !description.trim().isEmpty() ||
               keyAchievements != null && !keyAchievements.trim().isEmpty() ||
               technologiesUsed != null && !technologiesUsed.trim().isEmpty();
    }

    /**
     * Get years of experience as decimal.
     */
    public double getExperienceInYears() {
        return getDurationInMonths() / 12.0;
    }

    /**
     * Employment type enum.
     */
    public enum EmploymentType {
        FULL_TIME("Full-time"),
        PART_TIME("Part-time"),
        CONTRACT("Contract"),
        FREELANCE("Freelance"),
        TEMPORARY("Temporary"),
        INTERNSHIP("Internship"),
        VOLUNTEER("Volunteer"),
        CONSULTANT("Consultant"),
        OTHER("Other");

        private final String displayName;

        EmploymentType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static EmploymentType fromString(String value) {
            if (value == null) {
                return FULL_TIME;
            }
            
            String normalized = value.toLowerCase().trim();
            
            if (normalized.contains("full") || normalized.contains("permanent")) {
                return FULL_TIME;
            } else if (normalized.contains("part")) {
                return PART_TIME;
            } else if (normalized.contains("contract")) {
                return CONTRACT;
            } else if (normalized.contains("freelance")) {
                return FREELANCE;
            } else if (normalized.contains("temp")) {
                return TEMPORARY;
            } else if (normalized.contains("intern")) {
                return INTERNSHIP;
            } else if (normalized.contains("volunteer")) {
                return VOLUNTEER;
            } else if (normalized.contains("consult")) {
                return CONSULTANT;
            }
            
            return OTHER;
        }
    }
}