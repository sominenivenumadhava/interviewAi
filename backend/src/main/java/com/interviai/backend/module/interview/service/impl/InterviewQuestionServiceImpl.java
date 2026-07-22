package com.interviai.backend.module.interview.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviai.backend.common.exception.BusinessException;
import com.interviai.backend.common.exception.ResourceNotFoundException;
import com.interviai.backend.module.ai.service.AIService;
import com.interviai.backend.module.interview.dto.request.SubmitAnswerRequest;
import com.interviai.backend.module.interview.dto.response.InterviewAnswerResponse;
import com.interviai.backend.module.interview.dto.response.InterviewQuestionResponse;
import com.interviai.backend.module.interview.entity.Interview;
import com.interviai.backend.module.interview.entity.InterviewAnswer;
import com.interviai.backend.module.interview.entity.InterviewQuestion;
import com.interviai.backend.module.interview.enums.InterviewStatus;
import com.interviai.backend.module.interview.mapper.InterviewMapper;
import com.interviai.backend.module.interview.repository.InterviewAnswerRepository;
import com.interviai.backend.module.interview.repository.InterviewQuestionRepository;
import com.interviai.backend.module.interview.repository.InterviewRepository;
import com.interviai.backend.module.interview.service.InterviewQuestionService;
import com.interviai.backend.module.resume.entity.Resume;
import com.interviai.backend.module.user.entity.User;
import com.interviai.backend.module.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class InterviewQuestionServiceImpl implements InterviewQuestionService {
    
    private static final Logger log = LoggerFactory.getLogger(InterviewQuestionServiceImpl.class);
    
    @Autowired
    private InterviewRepository interviewRepository;
    
    @Autowired
    private InterviewQuestionRepository questionRepository;
    
    @Autowired
    private InterviewAnswerRepository answerRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AIService aiService;
    
    @Autowired
    private InterviewMapper interviewMapper;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public List<InterviewQuestionResponse> generateQuestions(String sessionId, UUID userId) {
        Interview interview = findInterviewBySessionIdAndUser(sessionId, userId);
        
        // Check if questions already exist
        if (!interview.getQuestions().isEmpty()) {
            return interviewMapper.toQuestionResponses(interview.getQuestions());
        }
        
        try {
            // Get configuration from interview
            Map<String, Object> config = objectMapper.readValue(
                interview.getConfiguration() != null ? interview.getConfiguration() : "{}", 
                Map.class
            );
            
            int numberOfQuestions = (int) config.getOrDefault("numberOfQuestions", 10);
            
            // Prepare resume content
            String resumeContent = "";
            if (interview.getResume() != null) {
                Resume resume = interview.getResume();
                resumeContent = buildResumeContent(resume);
            }
            
            // Generate questions using AI
            String questionsJson = aiService.generateInterviewQuestions(
                resumeContent,
                interview.getRole(),
                interview.getDifficultyLevel().name(),
                numberOfQuestions
            ).block(); // Blocking for simplicity in this implementation
            
            // Parse the generated questions
            Map<String, Object> generatedData = objectMapper.readValue(questionsJson, Map.class);
            List<Map<String, Object>> questionsList = (List<Map<String, Object>>) generatedData.get("questions");
            
            List<InterviewQuestion> questions = new ArrayList<>();
            for (int i = 0; i < questionsList.size(); i++) {
                Map<String, Object> questionData = questionsList.get(i);
                InterviewQuestion question = createQuestionFromData(interview, questionData, i + 1);
                questions.add(question);
            }
            
            // Save all questions
            List<InterviewQuestion> savedQuestions = questionRepository.saveAll(questions);
            
            return interviewMapper.toQuestionResponses(savedQuestions);
            
        } catch (Exception e) {
            log.error("Error generating questions for interview {}: {}", sessionId, e.getMessage());
            throw new BusinessException("Failed to generate interview questions");
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public InterviewQuestionResponse getQuestion(String sessionId, Integer questionOrder, UUID userId) {
        Interview interview = findInterviewBySessionIdAndUser(sessionId, userId);
        
        InterviewQuestion question = questionRepository.findByInterviewAndQuestionOrder(interview, questionOrder)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
        
        return interviewMapper.toQuestionResponse(question);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InterviewQuestionResponse> getAllQuestions(String sessionId, UUID userId) {
        Interview interview = findInterviewBySessionIdAndUser(sessionId, userId);
        
        List<InterviewQuestion> questions = questionRepository.findByInterviewWithAnswers(interview);
        
        return interviewMapper.toQuestionResponses(questions);
    }
    
    @Override
    public InterviewAnswerResponse submitAnswer(SubmitAnswerRequest request, UUID userId) {
        Interview interview = findInterviewBySessionIdAndUser(request.getSessionId(), userId);
        
        if (interview.getStatus() != InterviewStatus.IN_PROGRESS) {
            throw new BusinessException("Interview is not in progress");
        }
        
        InterviewQuestion question = questionRepository.findByInterviewAndQuestionOrder(
                interview, request.getQuestionOrder())
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
        
        // Check if answer already exists
        InterviewAnswer answer = answerRepository.findByQuestion(question)
                .orElse(new InterviewAnswer());
        
        // Update answer details
        answer.setInterview(interview);
        answer.setQuestion(question);
        answer.setAnswerText(request.getAnswerText());
        answer.setAnswerAudioUrl(request.getAnswerAudioUrl());
        answer.setAnswerVideoUrl(request.getAnswerVideoUrl());
        
        if (answer.getStartedAt() == null) {
            answer.startAnswering();
        }
        
        answer.submit();
        
        if (request.getTimeTakenSeconds() != null) {
            answer.setTimeTaken(Duration.ofSeconds(request.getTimeTakenSeconds()));
        }
        
        InterviewAnswer savedAnswer = answerRepository.save(answer);
        
        return interviewMapper.toAnswerResponse(savedAnswer);
    }
    
    @Override
    public InterviewAnswerResponse evaluateAnswer(String sessionId, Integer questionOrder, UUID userId) {
        Interview interview = findInterviewBySessionIdAndUser(sessionId, userId);
        
        InterviewQuestion question = questionRepository.findByInterviewAndQuestionOrder(
                interview, questionOrder)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
        
        InterviewAnswer answer = answerRepository.findByQuestion(question)
                .orElseThrow(() -> new ResourceNotFoundException("Answer not found"));
        
        if (answer.isEvaluated()) {
            return interviewMapper.toAnswerResponse(answer);
        }
        
        try {
            // Prepare evaluation criteria
            String criteria = String.join(", ", question.getEvaluationCriteria());
            
            // Get AI evaluation
            String evaluationJson = aiService.evaluateAnswer(
                question.getQuestionText(),
                answer.getAnswerText(),
                criteria
            ).block();
            
            // Parse evaluation result
            Map<String, Object> evaluation = objectMapper.readValue(evaluationJson, Map.class);
            
            // Update answer with evaluation
            answer.setScore(((Number) evaluation.get("score")).doubleValue());
            answer.setRating((String) evaluation.get("rating"));
            answer.setStrengths((List<String>) evaluation.get("strengths"));
            answer.setImprovements((List<String>) evaluation.get("improvements"));
            answer.setFeedback((String) evaluation.get("feedback"));
            answer.setSuggestedAnswer((String) evaluation.get("suggestedAnswer"));
            answer.setAiEvaluation(evaluationJson);
            
            // Calculate sub-scores if available
            if (evaluation.containsKey("confidenceScore")) {
                answer.setConfidenceScore(((Number) evaluation.get("confidenceScore")).doubleValue());
            }
            if (evaluation.containsKey("clarityScore")) {
                answer.setClarityScore(((Number) evaluation.get("clarityScore")).doubleValue());
            }
            if (evaluation.containsKey("relevanceScore")) {
                answer.setRelevanceScore(((Number) evaluation.get("relevanceScore")).doubleValue());
            }
            if (evaluation.containsKey("technicalAccuracyScore")) {
                answer.setTechnicalAccuracyScore(((Number) evaluation.get("technicalAccuracyScore")).doubleValue());
            }
            
            InterviewAnswer savedAnswer = answerRepository.save(answer);
            
            return interviewMapper.toAnswerResponse(savedAnswer);
            
        } catch (Exception e) {
            log.error("Error evaluating answer for question {}: {}", questionOrder, e.getMessage());
            throw new BusinessException("Failed to evaluate answer");
        }
    }
    
    @Override
    public List<InterviewAnswerResponse> evaluateAllAnswers(String sessionId, UUID userId) {
        Interview interview = findInterviewBySessionIdAndUser(sessionId, userId);
        
        List<InterviewAnswer> answers = answerRepository.findSubmittedAnswersByInterview(interview);
        
        List<InterviewAnswerResponse> evaluatedAnswers = new ArrayList<>();
        
        for (InterviewAnswer answer : answers) {
            if (!answer.isEvaluated()) {
                InterviewAnswerResponse evaluatedAnswer = evaluateAnswer(
                    sessionId, 
                    answer.getQuestion().getQuestionOrder(), 
                    userId
                );
                evaluatedAnswers.add(evaluatedAnswer);
            } else {
                evaluatedAnswers.add(interviewMapper.toAnswerResponse(answer));
            }
        }
        
        return evaluatedAnswers;
    }
    
    @Override
    @Transactional(readOnly = true)
    public InterviewQuestionResponse getNextQuestion(String sessionId, UUID userId) {
        Interview interview = findInterviewBySessionIdAndUser(sessionId, userId);
        
        List<InterviewQuestion> questions = questionRepository.findByInterviewOrderByQuestionOrder(interview);
        
        // Find the first unanswered question
        for (InterviewQuestion question : questions) {
            if (!question.isAnswered()) {
                return interviewMapper.toQuestionResponse(question);
            }
        }
        
        // If all questions are answered, return null
        return null;
    }
    
    @Override
    @Transactional(readOnly = true)
    public InterviewQuestionResponse getPreviousQuestion(String sessionId, Integer currentQuestionOrder, UUID userId) {
        Interview interview = findInterviewBySessionIdAndUser(sessionId, userId);
        
        if (currentQuestionOrder <= 1) {
            return null;
        }
        
        InterviewQuestion previousQuestion = questionRepository.findByInterviewAndQuestionOrder(
                interview, currentQuestionOrder - 1)
                .orElse(null);
        
        return previousQuestion != null ? interviewMapper.toQuestionResponse(previousQuestion) : null;
    }
    
    // Helper methods
    private Interview findInterviewBySessionIdAndUser(String sessionId, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return interviewRepository.findBySessionIdAndUser(sessionId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found"));
    }
    
    private String buildResumeContent(Resume resume) {
        StringBuilder content = new StringBuilder();
        
        if (resume.getSummary() != null) {
            content.append("Summary: ").append(resume.getSummary()).append("\n\n");
        }
        
        if (!resume.getSkills().isEmpty()) {
            content.append("Skills: ");
            content.append(resume.getSkills().stream()
                    .map(skill -> skill.getName())
                    .collect(Collectors.joining(", ")));
            content.append("\n\n");
        }
        
        if (!resume.getWorkExperiences().isEmpty()) {
            content.append("Experience:\n");
            resume.getWorkExperiences().forEach(exp -> {
                content.append("- ").append(exp.getJobTitle()).append(" at ").append(exp.getCompanyName())
                        .append(" (").append(exp.getStartDate()).append(" - ")
                        .append(exp.getEndDate() != null ? exp.getEndDate() : "Present").append(")\n");
                if (exp.getDescription() != null) {
                    content.append("  ").append(exp.getDescription()).append("\n");
                }
            });
            content.append("\n");
        }
        
        if (!resume.getEducations().isEmpty()) {
            content.append("Education:\n");
            resume.getEducations().forEach(edu -> {
                content.append("- ").append(edu.getDegree()).append(" in ").append(edu.getFieldOfStudy())
                        .append(" from ").append(edu.getInstitutionName()).append("\n");
            });
            content.append("\n");
        }
        
        return content.toString();
    }
    
    private InterviewQuestion createQuestionFromData(Interview interview, Map<String, Object> data, int order) {
        InterviewQuestion question = new InterviewQuestion();
        question.setInterview(interview);
        question.setQuestionOrder(order);
        question.setQuestionText((String) data.get("question"));
        question.setCategory((String) data.get("category"));
        
        String difficulty = (String) data.get("difficulty");
        if (difficulty != null) {
            question.setDifficultyLevel(interview.getDifficultyLevel());
        }
        
        if (data.containsKey("expectedTimeMinutes")) {
            question.setExpectedTimeMinutes(((Number) data.get("expectedTimeMinutes")).intValue());
        }
        
        if (data.containsKey("evaluationCriteria")) {
            question.setEvaluationCriteria((List<String>) data.get("evaluationCriteria"));
        }
        
        if (data.containsKey("followUpQuestions")) {
            question.setFollowUpQuestions((List<String>) data.get("followUpQuestions"));
        }
        
        question.setAiGenerated(true);
        
        return question;
    }
}
