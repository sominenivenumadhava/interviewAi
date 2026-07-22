package com.interviai.backend.module.interview.service;

import java.util.UUID;
import com.interviai.backend.module.interview.dto.request.SubmitAnswerRequest;
import java.util.UUID;
import com.interviai.backend.module.interview.dto.response.InterviewAnswerResponse;
import java.util.UUID;
import com.interviai.backend.module.interview.dto.response.InterviewQuestionResponse;

import java.util.List;

public interface InterviewQuestionService {
    
    List<InterviewQuestionResponse> generateQuestions(String sessionId, UUID userId);
    
    InterviewQuestionResponse getQuestion(String sessionId, Integer questionOrder, UUID userId);
    
    List<InterviewQuestionResponse> getAllQuestions(String sessionId, UUID userId);
    
    InterviewAnswerResponse submitAnswer(SubmitAnswerRequest request, UUID userId);
    
    InterviewAnswerResponse evaluateAnswer(String sessionId, Integer questionOrder, UUID userId);
    
    List<InterviewAnswerResponse> evaluateAllAnswers(String sessionId, UUID userId);
    
    InterviewQuestionResponse getNextQuestion(String sessionId, UUID userId);
    
    InterviewQuestionResponse getPreviousQuestion(String sessionId, Integer currentQuestionOrder, UUID userId);
}
