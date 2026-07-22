package com.interviai.backend.module.resume.entity;

import com.interviai.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Skill entity representing skills mentioned in resume.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Entity
@Table(name = "resume_skills", indexes = {
    @Index(name = "idx_resume_skills_resume_id", columnList = "resume_id"),
    @Index(name = "idx_resume_skills_category", columnList = "category"),
    @Index(name = "idx_resume_skills_proficiency_level", columnList = "proficiency_level")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Skill extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false, foreignKey = @ForeignKey(name = "fk_resume_skills_resume"))
    private Resume resume;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "category", length = 50)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SkillCategory category = SkillCategory.OTHER;

    @Column(name = "proficiency_level")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProficiencyLevel proficiencyLevel = ProficiencyLevel.INTERMEDIATE;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Column(name = "last_used_year")
    private Integer lastUsedYear;

    @Column(name = "is_certified", nullable = false)
    @Builder.Default
    private Boolean isCertified = false;

    @Column(name = "certification_name", length = 200)
    private String certificationName;

    @Column(name = "endorsements")
    private Integer endorsements;

    @Column(name = "self_assessed", nullable = false)
    @Builder.Default
    private Boolean selfAssessed = true;

    @Column(name = "priority_score")
    private Integer priorityScore;

    @Column(name = "matched_from_text", nullable = false)
    @Builder.Default
    private Boolean matchedFromText = true;

    @Column(name = "display_order")
    private Integer displayOrder;

    // Business Methods

    /**
     * Check if skill is certified.
     */
    public boolean isCertifiedSkill() {
        return Boolean.TRUE.equals(isCertified);
    }

    /**
     * Check if skill was recently used.
     */
    public boolean isRecentlyUsed() {
        if (lastUsedYear == null) {
            return false;
        }
        return java.time.Year.now().getValue() - lastUsedYear <= 2;
    }

    /**
     * Check if skill is advanced level or higher.
     */
    public boolean isAdvancedSkill() {
        return proficiencyLevel == ProficiencyLevel.ADVANCED || 
               proficiencyLevel == ProficiencyLevel.EXPERT;
    }

    /**
     * Get formatted proficiency display.
     */
    public String getFormattedProficiency() {
        StringBuilder sb = new StringBuilder(proficiencyLevel.getDisplayName());
        
        if (yearsOfExperience != null && yearsOfExperience > 0) {
            sb.append(" (").append(yearsOfExperience);
            sb.append(yearsOfExperience == 1 ? " year)" : " years)");
        }
        
        return sb.toString();
    }

    /**
     * Get skill importance score based on various factors.
     */
    public int getImportanceScore() {
        int score = 0;
        
        // Base score from proficiency level
        score += proficiencyLevel.getScore();
        
        // Years of experience bonus
        if (yearsOfExperience != null) {
            score += Math.min(yearsOfExperience * 10, 50);
        }
        
        // Recent usage bonus
        if (isRecentlyUsed()) {
            score += 20;
        }
        
        // Certification bonus
        if (isCertifiedSkill()) {
            score += 30;
        }
        
        // Category importance
        score += category.getImportanceScore();
        
        // Priority score if set
        if (priorityScore != null) {
            score += priorityScore;
        }
        
        return Math.min(score, 100);
    }

    /**
     * Skill category enum.
     */
    public enum SkillCategory {
        PROGRAMMING_LANGUAGE("Programming Language", 40),
        FRAMEWORK("Framework", 35),
        DATABASE("Database", 30),
        CLOUD("Cloud Platform", 35),
        DEVOPS("DevOps", 30),
        MOBILE("Mobile Development", 30),
        WEB("Web Development", 35),
        DATA_SCIENCE("Data Science", 40),
        MACHINE_LEARNING("Machine Learning", 45),
        CYBERSECURITY("Cybersecurity", 35),
        NETWORKING("Networking", 25),
        OPERATING_SYSTEM("Operating System", 20),
        SOFTWARE("Software Tool", 20),
        METHODOLOGY("Methodology", 15),
        SOFT_SKILL("Soft Skill", 10),
        LANGUAGE("Language", 10),
        OTHER("Other", 5);

        private final String displayName;
        private final int importanceScore;

        SkillCategory(String displayName, int importanceScore) {
            this.displayName = displayName;
            this.importanceScore = importanceScore;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getImportanceScore() {
            return importanceScore;
        }

        public static SkillCategory categorizeSkill(String skillName) {
            if (skillName == null) {
                return OTHER;
            }
            
            String normalized = skillName.toLowerCase().trim();
            
            // Programming languages
            if (normalized.matches(".*(java|python|javascript|typescript|c\\+\\+|c#|php|ruby|go|rust|swift|kotlin|scala|r).*")) {
                return PROGRAMMING_LANGUAGE;
            }
            
            // Frameworks
            if (normalized.matches(".*(spring|react|angular|vue|django|flask|laravel|rails|express|next\\.js|nuxt).*")) {
                return FRAMEWORK;
            }
            
            // Databases
            if (normalized.matches(".*(mysql|postgresql|mongodb|redis|elasticsearch|oracle|sql server|sqlite|cassandra).*")) {
                return DATABASE;
            }
            
            // Cloud platforms
            if (normalized.matches(".*(aws|azure|gcp|google cloud|kubernetes|docker|terraform).*")) {
                return CLOUD;
            }
            
            // DevOps
            if (normalized.matches(".*(jenkins|gitlab|github actions|ansible|puppet|chef|vagrant).*")) {
                return DEVOPS;
            }
            
            // Mobile
            if (normalized.matches(".*(android|ios|react native|flutter|xamarin|ionic).*")) {
                return MOBILE;
            }
            
            // Web development
            if (normalized.matches(".*(html|css|sass|less|webpack|gulp|grunt|bootstrap|tailwind).*")) {
                return WEB;
            }
            
            // Data Science / ML
            if (normalized.matches(".*(machine learning|deep learning|tensorflow|pytorch|pandas|numpy|scikit|spark|hadoop).*")) {
                return MACHINE_LEARNING;
            }
            
            // Software tools
            if (normalized.matches(".*(git|svn|jira|confluence|slack|photoshop|illustrator|figma|sketch).*")) {
                return SOFTWARE;
            }
            
            return OTHER;
        }
    }

    /**
     * Proficiency level enum.
     */
    public enum ProficiencyLevel {
        BEGINNER("Beginner", 10),
        INTERMEDIATE("Intermediate", 25),
        ADVANCED("Advanced", 40),
        EXPERT("Expert", 50);

        private final String displayName;
        private final int score;

        ProficiencyLevel(String displayName, int score) {
            this.displayName = displayName;
            this.score = score;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getScore() {
            return score;
        }

        public static ProficiencyLevel fromString(String value) {
            if (value == null) {
                return INTERMEDIATE;
            }
            
            String normalized = value.toLowerCase().trim();
            
            if (normalized.contains("beginner") || normalized.contains("novice") || normalized.contains("basic")) {
                return BEGINNER;
            } else if (normalized.contains("expert") || normalized.contains("master") || normalized.contains("senior")) {
                return EXPERT;
            } else if (normalized.contains("advanced") || normalized.contains("proficient")) {
                return ADVANCED;
            }
            
            return INTERMEDIATE;
        }
    }
}