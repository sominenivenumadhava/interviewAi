package com.interviai.backend.module.interview.mapper.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviai.backend.module.interview.dto.response.InterviewAnswerResponse;
import com.interviai.backend.module.interview.dto.response.InterviewQuestionResponse;
import com.interviai.backend.module.interview.dto.response.InterviewResponse;
import com.interviai.backend.module.interview.entity.Interview;
import com.interviai.backend.module.interview.entity.InterviewAnswer;
import com.interviai.backend.module.interview.entity.InterviewQuestion;
import com.interviai.backend.module.interview.mapper.InterviewMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class InterviewMapperImpl implements InterviewMapper {
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public InterviewResponse toResponse(Interview interview) {
        if (interview == null) {
            return null;
        }
        
        InterviewResponse response = new InterviewResponse();
        response.setId(interview.getId());
        response.setSessionId(interview.getSessionId());
        response.setTitle(interview.getTitle());
        response.setCompany(interview.getCompany());
        response.setRole(interview.getRole());
        response.setJobDescription(interview.getJobDescription());
        response.setInterviewType(interview.getInterviewType());
        response.setDifficultyLevel(interview.getDifficultyLevel());
        response.setStatus(interview.getStatus());
        response.setScheduledAt(interview.getScheduledAt());
        response.setStartedAt(interview.getStartedAt());
        response.setCompletedAt(interview.getCompletedAt());
        response.setDurationMinutes(interview.getDurationMinutes());
        
        if (interview.getActualDuration() != null) {
            response.setActualDurationSeconds(interview.getActualDuration().getSeconds());
        }
        
        response.setOverallScore(interview.getOverallScore());
        response.setOverallFeedback(interview.getOverallFeedback());
        
        // Parse strengths and improvements from JSON strings
        response.setStrengths(parseJsonList(interview.getStrengths()));
        response.setImprovements(parseJsonList(interview.getImprovements()));
        
        response.setTotalQuestions(interview.getQuestions().size());
        response.setAnsweredQuestions((int) interview.getQuestions().stream()
                .filter(q -> q.isAnswered())
                .count());
        
        response.setCreatedAt(interview.getCreatedAt());
        response.setUpdatedAt(interview.getUpdatedAt());
        
        return response;
    }
    
    @Override
    public InterviewQuestionResponse toQuestionResponse(InterviewQuestion question) {
        if (question == null) {
            return null;
        }
        
        InterviewQuestionResponse response = new InterviewQuestionResponse();
        response.setId(question.getId());
        response.setQuestionOrder(question.getQuestionOrder());
        response.setQuestionText(question.getQuestionText());
        response.setCategory(question.getCategory());
        response.setDifficultyLevel(question.getDifficultyLevel());
        response.setExpectedTimeMinutes(question.getExpectedTimeMinutes());
        response.setEvaluationCriteria(new ArrayList<>(question.getEvaluationCriteria()));
        response.setFollowUpQuestions(new ArrayList<>(question.getFollowUpQuestions()));
        response.setHints(question.getHints());
        response.setIsAnswered(question.isAnswered());
        
        if (question.getAnswer() != null) {
            response.setAnswer(toAnswerResponse(question.getAnswer()));
        }
        
        return response;
    }
    
    @Override
    public List<InterviewQuestionResponse> toQuestionResponses(List<InterviewQuestion> questions) {
        if (questions == null) {
            return new ArrayList<>();
        }
        
        return questions.stream()
                .map(this::toQuestionResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public InterviewAnswerResponse toAnswerResponse(InterviewAnswer answer) {
        if (answer == null) {
            return null;
        }
        
        InterviewAnswerResponse response = new InterviewAnswerResponse();
        response.setId(answer.getId());
        response.setAnswerText(answer.getAnswerText());
        response.setAnswerAudioUrl(answer.getAnswerAudioUrl());
        response.setAnswerVideoUrl(answer.getAnswerVideoUrl());
        response.setStartedAt(answer.getStartedAt());
        response.setSubmittedAt(answer.getSubmittedAt());
        
        if (answer.getTimeTaken() != null) {
            response.setTimeTakenSeconds(answer.getTimeTaken().getSeconds());
        }
        
        response.setScore(answer.getScore());
        response.setRating(answer.getRating());
        response.setStrengths(new ArrayList<>(answer.getStrengths()));
        response.setImprovements(new ArrayList<>(answer.getImprovements()));
        response.setFeedback(answer.getFeedback());
        response.setSuggestedAnswer(answer.getSuggestedAnswer());
        response.setConfidenceScore(answer.getConfidenceScore());
        response.setClarityScore(answer.getClarityScore());
        response.setRelevanceScore(answer.getRelevanceScore());
        response.setTechnicalAccuracyScore(answer.getTechnicalAccuracyScore());
        
        return response;
    }
    
    @Override
    public List<InterviewAnswerResponse> toAnswerResponses(List<InterviewAnswer> answers) {
        if (answers == null) {
            return new ArrayList<>();
        }
        
        return answers.stream()
                .map(this::toAnswerResponse)
                .collect(Collectors.toList());
    }
    
    private List<String> parseJsonList(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            return objectMapper.readValue(jsonString, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            // If not JSON, split by comma
            return List.of(jsonString.split(",\\s*"));
        }
    }
}