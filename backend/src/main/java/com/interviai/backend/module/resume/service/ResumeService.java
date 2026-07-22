package com.interviai.backend.module.resume.service;

import com.interviai.backend.common.dto.PageResponse;
import com.interviai.backend.module.resume.dto.request.ResumeUpdateRequest;
import com.interviai.backend.module.resume.dto.request.ResumeUploadRequest;
import com.interviai.backend.module.resume.dto.response.ResumeResponse;
import com.interviai.backend.module.resume.entity.Resume;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for resume management operations.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
public interface ResumeService {

    /**
     * Upload and process a new resume.
     */
    ResumeResponse uploadResume(UUID userId, ResumeUploadRequest request, MultipartFile file);

    /**
     * Get resume by ID for specific user.
     */
    Optional<ResumeResponse> getResumeById(UUID userId, UUID resumeId);

    /**
     * Get resume entity by ID for specific user.
     */
    Optional<Resume> getResumeEntityById(UUID userId, UUID resumeId);

    /**
     * Get all resumes for a user.
     */
    List<ResumeResponse> getUserResumes(UUID userId);

    /**
     * Get all resumes for a user with pagination.
     */
    PageResponse<ResumeResponse> getUserResumes(UUID userId, Pageable pageable);

    /**
     * Get primary resume for a user.
     */
    Optional<ResumeResponse> getPrimaryResume(UUID userId);

    /**
     * Update resume information.
     */
    ResumeResponse updateResume(UUID userId, UUID resumeId, ResumeUpdateRequest request);

    /**
     * Set resume as primary.
     */
    ResumeResponse setPrimaryResume(UUID userId, UUID resumeId);

    /**
     * Delete resume.
     */
    void deleteResume(UUID userId, UUID resumeId);

    /**
     * Search resumes by title.
     */
    PageResponse<ResumeResponse> searchResumesByTitle(UUID userId, String title, Pageable pageable);

    /**
     * Get resumes by processing status.
     */
    List<ResumeResponse> getResumesByStatus(UUID userId, Resume.ResumeStatus status);

    /**
     * Get resume processing status.
     */
    String getProcessingStatus(UUID userId, UUID resumeId);

    /**
     * Reprocess resume.
     */
    ResumeResponse reprocessResume(UUID userId, UUID resumeId);

    /**
     * Get resume file content.
     */
    byte[] getResumeFileContent(UUID userId, UUID resumeId);

    /**
     * Get resume extracted text.
     */
    String getResumeExtractedText(UUID userId, UUID resumeId);

    /**
     * Get user resume statistics.
     */
    ResumeStatistics getUserResumeStatistics(UUID userId);

    /**
     * Get resumes with high parsing confidence.
     */
    List<ResumeResponse> getHighQualityResumes(UUID userId, Double minConfidence);

    /**
     * Find resumes by skill mentioned.
     */
    List<ResumeResponse> findResumesBySkill(UUID userId, String skillName);

    /**
     * Find resumes by experience range.
     */
    List<ResumeResponse> findResumesByExperienceRange(UUID userId, Integer minMonths, Integer maxMonths);

    /**
     * Process resume asynchronously.
     */
    void processResumeAsync(UUID resumeId);

    /**
     * Check if user can upload more resumes.
     */
    boolean canUploadMoreResumes(UUID userId);

    /**
     * Get maximum resumes allowed per user.
     */
    int getMaxResumesPerUser();

    /**
     * Cleanup old failed resumes.
     */
    void cleanupOldFailedResumes();

    /**
     * Get stuck processing resumes.
     */
    List<Resume> getStuckProcessingResumes();

    /**
     * Resume statistics data class.
     */
    class ResumeStatistics {
        private final Long totalResumes;
        private final Long completedResumes;
        private final Long processingResumes;
        private final Long failedResumes;
        private final Long primaryResumes;
        private final Double averageConfidenceScore;
        private final Integer totalExperienceMonths;
        private final Integer totalSkills;
        private final Integer totalProjects;

        public ResumeStatistics(Long totalResumes, Long completedResumes, Long processingResumes,
                              Long failedResumes, Long primaryResumes, Double averageConfidenceScore,
                              Integer totalExperienceMonths, Integer totalSkills, Integer totalProjects) {
            this.totalResumes = totalResumes;
            this.completedResumes = completedResumes;
            this.processingResumes = processingResumes;
            this.failedResumes = failedResumes;
            this.primaryResumes = primaryResumes;
            this.averageConfidenceScore = averageConfidenceScore;
            this.totalExperienceMonths = totalExperienceMonths;
            this.totalSkills = totalSkills;
            this.totalProjects = totalProjects;
        }

        // Getters
        public Long getTotalResumes() { return totalResumes; }
        public Long getCompletedResumes() { return completedResumes; }
        public Long getProcessingResumes() { return processingResumes; }
        public Long getFailedResumes() { return failedResumes; }
        public Long getPrimaryResumes() { return primaryResumes; }
        public Double getAverageConfidenceScore() { return averageConfidenceScore; }
        public Integer getTotalExperienceMonths() { return totalExperienceMonths; }
        public Integer getTotalSkills() { return totalSkills; }
        public Integer getTotalProjects() { return totalProjects; }
    }
}