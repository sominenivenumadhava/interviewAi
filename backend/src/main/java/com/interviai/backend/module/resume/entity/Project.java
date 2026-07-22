package com.interviai.backend.module.resume.entity;

import com.interviai.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

/**
 * Project entity representing projects mentioned in resume.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Entity
@Table(name = "resume_projects", indexes = {
    @Index(name = "idx_resume_projects_resume_id", columnList = "resume_id"),
    @Index(name = "idx_resume_projects_project_type", columnList = "project_type"),
    @Index(name = "idx_resume_projects_end_date", columnList = "end_date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Project extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false, foreignKey = @ForeignKey(name = "fk_resume_projects_resume"))
    private Resume resume;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "role", length = 100)
    private String role;

    @Column(name = "organization", length = 200)
    private String organization;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_type", length = 30)
    @Builder.Default
    private ProjectType projectType = ProjectType.PERSONAL;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_ongoing", nullable = false)
    @Builder.Default
    private Boolean isOngoing = false;

    @Column(name = "technologies_used", columnDefinition = "TEXT")
    private String technologiesUsed;

    @Column(name = "key_achievements", columnDefinition = "TEXT")
    private String keyAchievements;

    @Column(name = "challenges_faced", columnDefinition = "TEXT")
    private String challengesFaced;

    @Column(name = "project_url", length = 500)
    private String projectUrl;

    @Column(name = "github_url", length = 500)
    private String githubUrl;

    @Column(name = "demo_url", length = 500)
    private String demoUrl;

    @Column(name = "team_size")
    private Integer teamSize;

    @Column(name = "budget_range", length = 50)
    private String budgetRange;

    @Column(name = "client_name", length = 200)
    private String clientName;

    @Column(name = "industry", length = 100)
    private String industry;

    @Column(name = "display_order")
    private Integer displayOrder;

    // Business Methods

    /**
     * Check if project is currently ongoing.
     */
    public boolean isCurrentlyWorking() {
        return Boolean.TRUE.equals(isOngoing);
    }

    /**
     * Get project duration in months.
     */
    public int getDurationInMonths() {
        if (startDate == null) {
            return 0;
        }
        
        LocalDate endDateToUse = isOngoing ? LocalDate.now() : endDate;
        if (endDateToUse == null) {
            return 0;
        }
        
        return (int) Period.between(startDate, endDateToUse).toTotalMonths();
    }

    /**
     * Get formatted project duration.
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
        
        if (isOngoing) {
            sb.append("Present");
        } else if (endDate != null) {
            sb.append(endDate.getMonthValue()).append("/").append(endDate.getYear());
        } else {
            sb.append("Unknown");
        }
        
        return sb.toString();
    }

    /**
     * Check if project has external links.
     */
    public boolean hasExternalLinks() {
        return (projectUrl != null && !projectUrl.trim().isEmpty()) ||
               (githubUrl != null && !githubUrl.trim().isEmpty()) ||
               (demoUrl != null && !demoUrl.trim().isEmpty());
    }

    /**
     * Get all external links.
     */
    public List<String> getExternalLinks() {
        return List.of(projectUrl, githubUrl, demoUrl)
                .stream()
                .filter(url -> url != null && !url.trim().isEmpty())
                .toList();
    }

    /**
     * Check if project has detailed information.
     */
    public boolean hasDetailedInfo() {
        return (description != null && !description.trim().isEmpty()) ||
               (keyAchievements != null && !keyAchievements.trim().isEmpty()) ||
               (technologiesUsed != null && !technologiesUsed.trim().isEmpty());
    }

    /**
     * Get project complexity score.
     */
    public int getComplexityScore() {
        int score = 0;
        
        // Duration bonus
        int months = getDurationInMonths();
        score += Math.min(months * 2, 20);
        
        // Team size bonus
        if (teamSize != null) {
            if (teamSize == 1) {
                score += 5; // Solo project
            } else if (teamSize <= 5) {
                score += 10; // Small team
            } else {
                score += 15; // Large team
            }
        }
        
        // Type bonus
        score += projectType.getComplexityScore();
        
        // External links bonus
        if (hasExternalLinks()) {
            score += 10;
        }
        
        // Detailed info bonus
        if (hasDetailedInfo()) {
            score += 10;
        }
        
        return Math.min(score, 100);
    }

    /**
     * Extract technologies as list.
     */
    public List<String> getTechnologiesList() {
        if (technologiesUsed == null || technologiesUsed.trim().isEmpty()) {
            return List.of();
        }
        
        return List.of(technologiesUsed.split("[,;\\n]"))
                .stream()
                .map(String::trim)
                .filter(tech -> !tech.isEmpty())
                .toList();
    }

    /**
     * Project type enum.
     */
    public enum ProjectType {
        PERSONAL("Personal Project", 10),
        ACADEMIC("Academic Project", 15),
        PROFESSIONAL("Professional Project", 25),
        OPEN_SOURCE("Open Source Project", 20),
        FREELANCE("Freelance Project", 20),
        STARTUP("Startup Project", 30),
        RESEARCH("Research Project", 25),
        HACKATHON("Hackathon Project", 15),
        CLIENT("Client Project", 25),
        OTHER("Other", 10);

        private final String displayName;
        private final int complexityScore;

        ProjectType(String displayName, int complexityScore) {
            this.displayName = displayName;
            this.complexityScore = complexityScore;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getComplexityScore() {
            return complexityScore;
        }

        public static ProjectType fromString(String value) {
            if (value == null) {
                return PERSONAL;
            }
            
            String normalized = value.toLowerCase().trim();
            
            if (normalized.contains("academic") || normalized.contains("university") || normalized.contains("college")) {
                return ACADEMIC;
            } else if (normalized.contains("professional") || normalized.contains("work") || normalized.contains("company")) {
                return PROFESSIONAL;
            } else if (normalized.contains("open source") || normalized.contains("opensource")) {
                return OPEN_SOURCE;
            } else if (normalized.contains("freelance") || normalized.contains("contract")) {
                return FREELANCE;
            } else if (normalized.contains("startup")) {
                return STARTUP;
            } else if (normalized.contains("research")) {
                return RESEARCH;
            } else if (normalized.contains("hackathon") || normalized.contains("competition")) {
                return HACKATHON;
            } else if (normalized.contains("client")) {
                return CLIENT;
            } else if (normalized.contains("personal") || normalized.contains("hobby")) {
                return PERSONAL;
            }
            
            return OTHER;
        }
    }
}