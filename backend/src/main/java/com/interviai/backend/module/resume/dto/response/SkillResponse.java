package com.interviai.backend.module.resume.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Response DTO for Skill entity.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillResponse {

    private UUID id;
    
    private String name;
    
    private String category;
    
    private String proficiencyLevel;
    
    private Integer yearsOfExperience;
    
    private Integer lastUsedYear;
    
    private Boolean isCertified;
    
    private String certificationName;
    
    private Integer endorsements;
    
    private Boolean selfAssessed;
    
    private Integer priorityScore;
    
    private Boolean matchedFromText;
    
    private Integer displayOrder;
    
    // Computed fields
    private String formattedProficiency;
    
    private Integer importanceScore;
    
    private Boolean isAdvancedSkill;
    
    private Boolean isRecentlyUsed;
}