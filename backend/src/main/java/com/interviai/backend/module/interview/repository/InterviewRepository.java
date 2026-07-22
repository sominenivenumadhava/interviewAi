package com.interviai.backend.module.interview.repository;

import com.interviai.backend.module.interview.entity.Interview;
import com.interviai.backend.module.interview.enums.InterviewStatus;
import com.interviai.backend.module.interview.enums.InterviewType;
import com.interviai.backend.module.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, UUID> {
    
    Optional<Interview> findBySessionId(String sessionId);
    
    Optional<Interview> findByIdAndUser(UUID id, User user);
    
    Optional<Interview> findBySessionIdAndUser(String sessionId, User user);
    
    Page<Interview> findByUser(User user, Pageable pageable);
    
    Page<Interview> findByUserAndStatus(User user, InterviewStatus status, Pageable pageable);
    
    Page<Interview> findByUserAndInterviewType(User user, InterviewType type, Pageable pageable);
    
    List<Interview> findByUserAndStatusIn(User user, List<InterviewStatus> statuses);
    
    @Query("SELECT i FROM Interview i WHERE i.user = :user AND i.status = :status ORDER BY i.scheduledAt DESC")
    List<Interview> findUpcomingInterviews(@Param("user") User user, @Param("status") InterviewStatus status);
    
    @Query("SELECT i FROM Interview i WHERE i.user = :user AND i.completedAt BETWEEN :startDate AND :endDate")
    List<Interview> findCompletedInterviewsBetween(@Param("user") User user, 
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(i) FROM Interview i WHERE i.user = :user AND i.status = :status")
    long countByUserAndStatus(@Param("user") User user, @Param("status") InterviewStatus status);
    
    @Query("SELECT AVG(i.overallScore) FROM Interview i WHERE i.user = :user AND i.status = 'COMPLETED'")
    Double getAverageScoreByUser(@Param("user") User user);
    
    @Query("SELECT i FROM Interview i LEFT JOIN FETCH i.questions WHERE i.sessionId = :sessionId")
    Optional<Interview> findBySessionIdWithQuestions(@Param("sessionId") String sessionId);
    
    @Query("SELECT i FROM Interview i LEFT JOIN FETCH i.questions LEFT JOIN FETCH i.answers WHERE i.sessionId = :sessionId")
    Optional<Interview> findBySessionIdWithQuestionsAndAnswers(@Param("sessionId") String sessionId);
    
    @Query("SELECT DISTINCT i.role FROM Interview i WHERE i.user = :user ORDER BY i.role")
    List<String> findDistinctRolesByUser(@Param("user") User user);
    
    @Query("SELECT DISTINCT i.company FROM Interview i WHERE i.user = :user AND i.company IS NOT NULL ORDER BY i.company")
    List<String> findDistinctCompaniesByUser(@Param("user") User user);
    
    boolean existsByUserAndSessionId(User user, String sessionId);
}