package com.interviai.backend.module.resume.repository;

import com.interviai.backend.module.resume.entity.Resume;
import com.interviai.backend.module.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Resume entity.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Repository
public interface ResumeRepository extends JpaRepository<Resume, UUID> {

    /**
     * Find all resumes by user.
     */
    List<Resume> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Find all resumes by user with pagination.
     */
    Page<Resume> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * Find resume by user and resume ID.
     */
    Optional<Resume> findByIdAndUser(UUID id, User user);

    /**
     * Find primary resume of a user.
     */
    Optional<Resume> findByUserAndIsPrimaryTrue(User user);

    /**
     * Find all completed resumes by user.
     */
    List<Resume> findByUserAndStatusOrderByCreatedAtDesc(User user, Resume.ResumeStatus status);

    /**
     * Find resumes by status.
     */
    List<Resume> findByStatusOrderByCreatedAtAsc(Resume.ResumeStatus status);

    /**
     * Find resumes that are being processed for too long (stuck processing).
     */
    @Query("SELECT r FROM Resume r WHERE r.status = 'PROCESSING' AND r.processingStartedAt < :cutoffTime")
    List<Resume> findStuckProcessingResumes(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Count resumes by user.
     */
    long countByUser(User user);

    /**
     * Count resumes by user and status.
     */
    long countByUserAndStatus(User user, Resume.ResumeStatus status);

    /**
     * Check if user has any resumes.
     */
    boolean existsByUser(User user);

    /**
     * Check if user has primary resume.
     */
    boolean existsByUserAndIsPrimaryTrue(User user);

    /**
     * Find resumes by title containing text (case-insensitive).
     */
    Page<Resume> findByUserAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
            User user, String title, Pageable pageable);

    /**
     * Find resumes created within date range.
     */
    @Query("SELECT r FROM Resume r WHERE r.user = :user AND r.createdAt BETWEEN :startDate AND :endDate ORDER BY r.createdAt DESC")
    List<Resume> findByUserAndDateRange(
            @Param("user") User user,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find latest resume by user.
     */
    Optional<Resume> findFirstByUserOrderByCreatedAtDesc(User user);

    /**
     * Unset primary flag for all user resumes.
     */
    @Modifying
    @Query("UPDATE Resume r SET r.isPrimary = false WHERE r.user = :user")
    void unsetPrimaryForAllUserResumes(@Param("user") User user);

    /**
     * Set resume as primary.
     */
    @Modifying
    @Query("UPDATE Resume r SET r.isPrimary = true WHERE r.id = :resumeId")
    void setPrimaryResume(@Param("resumeId") UUID resumeId);

    /**
     * Update resume processing status.
     */
    @Modifying
    @Query("UPDATE Resume r SET r.status = :status, r.processingCompletedAt = :completedAt, r.processingError = :error WHERE r.id = :resumeId")
    void updateProcessingStatus(
            @Param("resumeId") UUID resumeId,
            @Param("status") Resume.ResumeStatus status,
            @Param("completedAt") LocalDateTime completedAt,
            @Param("error") String error);

    /**
     * Update resume parsing results.
     */
    @Modifying
    @Query("UPDATE Resume r SET r.extractedText = :extractedText, r.parsedContent = :parsedContent, " +
           "r.fullName = :fullName, r.email = :email, r.phone = :phone, r.location = :location, " +
           "r.summary = :summary, r.totalExperienceYears = :expYears, r.totalExperienceMonths = :expMonths, " +
           "r.parsingConfidenceScore = :confidenceScore WHERE r.id = :resumeId")
    void updateParsingResults(
            @Param("resumeId") UUID resumeId,
            @Param("extractedText") String extractedText,
            @Param("parsedContent") String parsedContent,
            @Param("fullName") String fullName,
            @Param("email") String email,
            @Param("phone") String phone,
            @Param("location") String location,
            @Param("summary") String summary,
            @Param("expYears") Integer expYears,
            @Param("expMonths") Integer expMonths,
            @Param("confidenceScore") Double confidenceScore);

    /**
     * Find resumes with high parsing confidence.
     */
    @Query("SELECT r FROM Resume r WHERE r.user = :user AND r.status = 'COMPLETED' AND r.parsingConfidenceScore >= :minConfidence ORDER BY r.parsingConfidenceScore DESC")
    List<Resume> findHighQualityResumes(
            @Param("user") User user,
            @Param("minConfidence") Double minConfidence);

    /**
     * Get user resume statistics.
     */
    @Query("SELECT " +
           "COUNT(r) as total, " +
           "COUNT(CASE WHEN r.status = 'COMPLETED' THEN 1 END) as completed, " +
           "COUNT(CASE WHEN r.status = 'PROCESSING' THEN 1 END) as processing, " +
           "COUNT(CASE WHEN r.status = 'FAILED' THEN 1 END) as failed, " +
           "COUNT(CASE WHEN r.isPrimary = true THEN 1 END) as primary " +
           "FROM Resume r WHERE r.user = :user")
    ResumeStats getUserResumeStats(@Param("user") User user);

    /**
     * Find resumes by file size range.
     */
    @Query("SELECT r FROM Resume r WHERE r.user = :user AND r.fileSize BETWEEN :minSize AND :maxSize ORDER BY r.createdAt DESC")
    List<Resume> findByFileSizeRange(
            @Param("user") User user,
            @Param("minSize") Long minSize,
            @Param("maxSize") Long maxSize);

    /**
     * Find resumes with specific skills mentioned.
     */
    @Query("SELECT DISTINCT r FROM Resume r JOIN r.skills s WHERE r.user = :user AND LOWER(s.name) LIKE LOWER(CONCAT('%', :skillName, '%'))")
    List<Resume> findBySkillMentioned(@Param("user") User user, @Param("skillName") String skillName);

    /**
     * Find resumes by experience range.
     */
    @Query("SELECT r FROM Resume r WHERE r.user = :user AND " +
           "((r.totalExperienceYears * 12 + COALESCE(r.totalExperienceMonths, 0)) BETWEEN :minMonths AND :maxMonths) " +
           "ORDER BY r.totalExperienceYears DESC, r.totalExperienceMonths DESC")
    List<Resume> findByExperienceRange(
            @Param("user") User user,
            @Param("minMonths") Integer minMonths,
            @Param("maxMonths") Integer maxMonths);

    /**
     * Delete old failed resumes.
     */
    @Modifying
    @Query("DELETE FROM Resume r WHERE r.status = 'FAILED' AND r.createdAt < :cutoffDate")
    void deleteOldFailedResumes(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Resume statistics interface for native query results.
     */
    interface ResumeStats {
        Long getTotal();
        Long getCompleted();
        Long getProcessing();
        Long getFailed();
        Long getPrimary();
    }
}