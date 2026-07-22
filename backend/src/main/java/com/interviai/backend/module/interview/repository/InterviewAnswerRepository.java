package com.interviai.backend.module.interview.repository;

import com.interviai.backend.module.interview.entity.InterviewAnswer;
import com.interviai.backend.module.interview.entity.Interview;
import com.interviai.backend.module.interview.entity.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InterviewAnswerRepository extends JpaRepository<InterviewAnswer, UUID> {
    
    Optional<InterviewAnswer> findByQuestion(InterviewQuestion question);
    
    List<InterviewAnswer> findByInterview(Interview interview);
    
    List<InterviewAnswer> findByInterviewAndScoreIsNotNull(Interview interview);
    
    @Query("SELECT a FROM InterviewAnswer a WHERE a.interview = :interview AND a.submittedAt IS NOT NULL ORDER BY a.question.questionOrder")
    List<InterviewAnswer> findSubmittedAnswersByInterview(@Param("interview") Interview interview);
    
    @Query("SELECT AVG(a.score) FROM InterviewAnswer a WHERE a.interview = :interview AND a.score IS NOT NULL")
    Double getAverageScoreByInterview(@Param("interview") Interview interview);
    
    @Query("SELECT COUNT(a) FROM InterviewAnswer a WHERE a.interview = :interview AND a.score >= :minScore")
    long countAnswersAboveScore(@Param("interview") Interview interview, @Param("minScore") Double minScore);
    
    boolean existsByQuestion(InterviewQuestion question);
}