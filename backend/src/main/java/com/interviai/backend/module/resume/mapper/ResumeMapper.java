package com.interviai.backend.module.resume.mapper;

import com.interviai.backend.module.resume.dto.response.*;
import com.interviai.backend.module.resume.entity.*;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for Resume entities and DTOs.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Component
public class ResumeMapper {

    /**
     * Convert Resume entity to ResumeResponse DTO.
     */
    public ResumeResponse toResumeResponse(Resume resume) {
        if (resume == null) {
            return null;
        }

        return ResumeResponse.builder()
                .id(resume.getId())
                .title(resume.getTitle())
                .originalFilename(resume.getOriginalFilename())
                .fileSize(resume.getFileSize())
                .formattedFileSize(resume.getFormattedFileSize())
                .contentType(resume.getContentType())
                .fullName(resume.getFullName())
                .email(resume.getEmail())
                .phone(resume.getPhone())
                .location(resume.getLocation())
                .summary(resume.getSummary())
                .totalExperienceYears(resume.getTotalExperienceYears())
                .totalExperienceMonths(resume.getTotalExperienceMonths())
                .formattedExperience(resume.getFormattedExperience())
                .status(resume.getStatus().getDisplayName())
                .isPrimary(resume.getIsPrimary())
                .processingStartedAt(resume.getProcessingStartedAt())
                .processingCompletedAt(resume.getProcessingCompletedAt())
                .processingError(resume.getProcessingError())
                .parsingConfidenceScore(resume.getParsingConfidenceScore())
                .createdAt(resume.getCreatedAt())
                .updatedAt(resume.getUpdatedAt())
                .educations(toEducationResponseList(resume.getEducations()))
                .workExperiences(toWorkExperienceResponseList(resume.getWorkExperiences()))
                .skills(toSkillResponseList(resume.getSkills()))
                .projects(toProjectResponseList(resume.getProjects()))
                .isProcessingCompleted(resume.isProcessingCompleted())
                .isProcessingFailed(resume.isProcessingFailed())
                .isProcessing(resume.isProcessing())
                .processingDuration(calculateProcessingDuration(resume))
                .profileCompleteness(calculateProfileCompleteness(resume))
                .build();
    }

    /**
     * Convert Resume entity to ResumeResponse DTO without related entities.
     */
    public ResumeResponse toResumeResponseBasic(Resume resume) {
        if (resume == null) {
            return null;
        }

        return ResumeResponse.builder()
                .id(resume.getId())
                .title(resume.getTitle())
                .originalFilename(resume.getOriginalFilename())
                .fileSize(resume.getFileSize())
                .formattedFileSize(resume.getFormattedFileSize())
                .contentType(resume.getContentType())
                .fullName(resume.getFullName())
                .email(resume.getEmail())
                .phone(resume.getPhone())
                .location(resume.getLocation())
                .summary(resume.getSummary())
                .totalExperienceYears(resume.getTotalExperienceYears())
                .totalExperienceMonths(resume.getTotalExperienceMonths())
                .formattedExperience(resume.getFormattedExperience())
                .status(resume.getStatus().getDisplayName())
                .isPrimary(resume.getIsPrimary())
                .processingStartedAt(resume.getProcessingStartedAt())
                .processingCompletedAt(resume.getProcessingCompletedAt())
                .processingError(resume.getProcessingError())
                .parsingConfidenceScore(resume.getParsingConfidenceScore())
                .createdAt(resume.getCreatedAt())
                .updatedAt(resume.getUpdatedAt())
                .isProcessingCompleted(resume.isProcessingCompleted())
                .isProcessingFailed(resume.isProcessingFailed())
                .isProcessing(resume.isProcessing())
                .processingDuration(calculateProcessingDuration(resume))
                .profileCompleteness(calculateProfileCompleteness(resume))
                .build();
    }

    /**
     * Convert Education entity to EducationResponse DTO.
     */
    public EducationResponse toEducationResponse(Education education) {
        if (education == null) {
            return null;
        }

        return EducationResponse.builder()
                .id(education.getId())
                .institutionName(education.getInstitutionName())
                .degree(education.getDegree())
                .fieldOfStudy(education.getFieldOfStudy())
                .degreeType(education.getDegreeType().getDisplayName())
                .startDate(education.getStartDate())
                .endDate(education.getEndDate())
                .isCurrent(education.getIsCurrent())
                .gpa(education.getGpa())
                .maxGpa(education.getMaxGpa())
                .formattedGpa(education.getFormattedGpa())
                .percentage(education.getPercentage())
                .grade(education.getGrade())
                .location(education.getLocation())
                .description(education.getDescription())
                .honors(education.getHonors())
                .relevantCoursework(education.getRelevantCoursework())
                .activities(education.getActivities())
                .displayOrder(education.getDisplayOrder())
                .formattedDateRange(education.getFormattedDateRange())
                .fullDegreeName(education.getFullDegreeName())
                .durationInYears(education.getDurationInYears())
                .hasPerformanceMetrics(education.hasPerformanceMetrics())
                .build();
    }

    /**
     * Convert WorkExperience entity to WorkExperienceResponse DTO.
     */
    public WorkExperienceResponse toWorkExperienceResponse(WorkExperience workExperience) {
        if (workExperience == null) {
            return null;
        }

        return WorkExperienceResponse.builder()
                .id(workExperience.getId())
                .companyName(workExperience.getCompanyName())
                .jobTitle(workExperience.getJobTitle())
                .employmentType(workExperience.getEmploymentType().getDisplayName())
                .location(workExperience.getLocation())
                .startDate(workExperience.getStartDate())
                .endDate(workExperience.getEndDate())
                .isCurrent(workExperience.getIsCurrent())
                .description(workExperience.getDescription())
                .keyAchievements(workExperience.getKeyAchievements())
                .technologiesUsed(workExperience.getTechnologiesUsed())
                .industry(workExperience.getIndustry())
                .companySize(workExperience.getCompanySize())
                .reportingTo(workExperience.getReportingTo())
                .teamSize(workExperience.getTeamSize())
                .salaryRange(workExperience.getSalaryRange())
                .displayOrder(workExperience.getDisplayOrder())
                .formattedDateRange(workExperience.getFormattedDateRange())
                .formattedDuration(workExperience.getFormattedDuration())
                .durationInMonths(workExperience.getDurationInMonths())
                .experienceInYears(workExperience.getExperienceInYears())
                .fullPositionTitle(workExperience.getFullPositionTitle())
                .hasDetailedInfo(workExperience.hasDetailedInfo())
                .build();
    }

    /**
     * Convert Skill entity to SkillResponse DTO.
     */
    public SkillResponse toSkillResponse(Skill skill) {
        if (skill == null) {
            return null;
        }

        return SkillResponse.builder()
                .id(skill.getId())
                .name(skill.getName())
                .category(skill.getCategory().getDisplayName())
                .proficiencyLevel(skill.getProficiencyLevel().getDisplayName())
                .yearsOfExperience(skill.getYearsOfExperience())
                .lastUsedYear(skill.getLastUsedYear())
                .isCertified(skill.getIsCertified())
                .certificationName(skill.getCertificationName())
                .endorsements(skill.getEndorsements())
                .selfAssessed(skill.getSelfAssessed())
                .priorityScore(skill.getPriorityScore())
                .matchedFromText(skill.getMatchedFromText())
                .displayOrder(skill.getDisplayOrder())
                .formattedProficiency(skill.getFormattedProficiency())
                .importanceScore(skill.getImportanceScore())
                .isAdvancedSkill(skill.isAdvancedSkill())
                .isRecentlyUsed(skill.isRecentlyUsed())
                .build();
    }

    /**
     * Convert Project entity to ProjectResponse DTO.
     */
    public ProjectResponse toProjectResponse(Project project) {
        if (project == null) {
            return null;
        }

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .role(project.getRole())
                .organization(project.getOrganization())
                .projectType(project.getProjectType().getDisplayName())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .isOngoing(project.getIsOngoing())
                .technologiesUsed(project.getTechnologiesUsed())
                .keyAchievements(project.getKeyAchievements())
                .challengesFaced(project.getChallengesFaced())
                .projectUrl(project.getProjectUrl())
                .githubUrl(project.getGithubUrl())
                .demoUrl(project.getDemoUrl())
                .teamSize(project.getTeamSize())
                .budgetRange(project.getBudgetRange())
                .clientName(project.getClientName())
                .industry(project.getIndustry())
                .displayOrder(project.getDisplayOrder())
                .formattedDateRange(project.getFormattedDateRange())
                .formattedDuration(project.getFormattedDuration())
                .durationInMonths(project.getDurationInMonths())
                .hasExternalLinks(project.hasExternalLinks())
                .externalLinks(project.getExternalLinks())
                .hasDetailedInfo(project.hasDetailedInfo())
                .complexityScore(project.getComplexityScore())
                .technologiesList(project.getTechnologiesList())
                .build();
    }

    /**
     * Convert list of Education entities to list of EducationResponse DTOs.
     */
    public List<EducationResponse> toEducationResponseList(List<Education> educations) {
        if (educations == null) {
            return null;
        }
        return educations.stream()
                .map(this::toEducationResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert list of WorkExperience entities to list of WorkExperienceResponse DTOs.
     */
    public List<WorkExperienceResponse> toWorkExperienceResponseList(List<WorkExperience> workExperiences) {
        if (workExperiences == null) {
            return null;
        }
        return workExperiences.stream()
                .map(this::toWorkExperienceResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert list of Skill entities to list of SkillResponse DTOs.
     */
    public List<SkillResponse> toSkillResponseList(List<Skill> skills) {
        if (skills == null) {
            return null;
        }
        return skills.stream()
                .map(this::toSkillResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert list of Project entities to list of ProjectResponse DTOs.
     */
    public List<ProjectResponse> toProjectResponseList(List<Project> projects) {
        if (projects == null) {
            return null;
        }
        return projects.stream()
                .map(this::toProjectResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert list of Resume entities to list of ResumeResponse DTOs.
     */
    public List<ResumeResponse> toResumeResponseList(List<Resume> resumes) {
        if (resumes == null) {
            return null;
        }
        return resumes.stream()
                .map(this::toResumeResponseBasic)
                .collect(Collectors.toList());
    }

    /**
     * Calculate processing duration string.
     */
    private String calculateProcessingDuration(Resume resume) {
        if (resume.getProcessingStartedAt() == null) {
            return null;
        }

        LocalDateTime endTime = resume.getProcessingCompletedAt() != null 
            ? resume.getProcessingCompletedAt() 
            : LocalDateTime.now();

        Duration duration = Duration.between(resume.getProcessingStartedAt(), endTime);
        
        long minutes = duration.toMinutes();
        if (minutes < 1) {
            return "Less than 1 minute";
        } else if (minutes < 60) {
            return minutes + " minute" + (minutes == 1 ? "" : "s");
        } else {
            long hours = duration.toHours();
            return hours + " hour" + (hours == 1 ? "" : "s");
        }
    }

    /**
     * Calculate profile completeness percentage.
     */
    private Integer calculateProfileCompleteness(Resume resume) {
        if (resume == null) {
            return 0;
        }

        int completeness = 0;
        int maxScore = 100;

        // Basic information (30%)
        if (resume.getFullName() != null && !resume.getFullName().trim().isEmpty()) {
            completeness += 5;
        }
        if (resume.getEmail() != null && !resume.getEmail().trim().isEmpty()) {
            completeness += 5;
        }
        if (resume.getPhone() != null && !resume.getPhone().trim().isEmpty()) {
            completeness += 5;
        }
        if (resume.getLocation() != null && !resume.getLocation().trim().isEmpty()) {
            completeness += 5;
        }
        if (resume.getSummary() != null && !resume.getSummary().trim().isEmpty()) {
            completeness += 10;
        }

        // Experience information (25%)
        if (resume.getWorkExperiences() != null && !resume.getWorkExperiences().isEmpty()) {
            completeness += 15;
            if (resume.getWorkExperiences().size() > 1) {
                completeness += 5; // Bonus for multiple experiences
            }
            if (resume.getTotalExperienceYears() != null && resume.getTotalExperienceYears() > 0) {
                completeness += 5;
            }
        }

        // Education information (20%)
        if (resume.getEducations() != null && !resume.getEducations().isEmpty()) {
            completeness += 15;
            if (resume.getEducations().size() > 1) {
                completeness += 5; // Bonus for multiple educations
            }
        }

        // Skills information (15%)
        if (resume.getSkills() != null && !resume.getSkills().isEmpty()) {
            completeness += 10;
            if (resume.getSkills().size() >= 5) {
                completeness += 5; // Bonus for 5+ skills
            }
        }

        // Projects information (10%)
        if (resume.getProjects() != null && !resume.getProjects().isEmpty()) {
            completeness += 10;
        }

        return Math.min(completeness, maxScore);
    }
}