package com.interviai.backend.module.interview.repository;

import com.interviai.backend.module.interview.entity.InterviewQuestion;
import com.interviai.backend.module.interview.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, UUID> {
    
    List<InterviewQuestion> findByInterviewOrderByQuestionOrder(Interview interview);
    
    Optional<InterviewQuestion> findByInterviewAndQuestionOrder(Interview interview, Integer questionOrder);
    
    @Query("SELECT q FROM InterviewQuestion q LEFT JOIN FETCH q.answer WHERE q.interview = :interview ORDER BY q.questionOrder")
    List<InterviewQuestion> findByInterviewWithAnswers(@Param("interview") Interview interview);
    
    @Query("SELECT COUNT(q) FROM InterviewQuestion q WHERE q.interview = :interview")
    long countByInterview(@Param("interview") Interview interview);
    
    @Query("SELECT COUNT(q) FROM InterviewQuestion q JOIN q.answer a WHERE q.interview = :interview AND a.submittedAt IS NOT NULL")
    long countAnsweredQuestions(@Param("interview") Interview interview);
    
    @Query("SELECT AVG(a.score) FROM InterviewQuestion q JOIN q.answer a WHERE q.interview = :interview AND a.score IS NOT NULL")
    Double getAverageScoreByInterview(@Param("interview") Interview interview);
    
    @Query("SELECT q FROM InterviewQuestion q WHERE q.interview = :interview AND q.category = :category ORDER BY q.questionOrder")
    List<InterviewQuestion> findByInterviewAndCategory(@Param("interview") Interview interview, @Param("category") String category);
    
    boolean existsByInterviewAndQuestionOrder(Interview interview, Integer questionOrder);
}