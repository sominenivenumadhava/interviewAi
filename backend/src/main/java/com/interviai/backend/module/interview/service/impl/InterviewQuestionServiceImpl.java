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
            
            int numberOfQuestions = ((Number) config.getOrDefault("numberOfQuestions", 10)).intValue();
            
            // Prepare resume content
            String resumeContent = "";
            if (interview.getResume() != null) {
                Resume resume = interview.getResume();
                resumeContent = buildResumeContent(resume);
            }

            String interviewContext = buildInterviewContext(interview, config, resumeContent);
            
            // Generate questions using AI
            String questionsJson = aiService.generateInterviewQuestions(
                interviewContext,
                interview.getRole(),
                interview.getDifficultyLevel().name(),
                numberOfQuestions
            ).block(); // Blocking for simplicity in this implementation
            
            // Parse the generated questions
            Map<String, Object> generatedData = objectMapper.readValue(extractJsonObject(questionsJson), Map.class);
            List<Map<String, Object>> questionsList = (List<Map<String, Object>>) generatedData.get("questions");
            if (questionsList == null || questionsList.isEmpty()) {
                throw new BusinessException("AI returned no interview questions");
            }
            
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
            log.warn("Error generating AI questions for interview {}: {}. Generating fallback questions.", sessionId, e.getMessage());
            try {
                Map<String, Object> config = objectMapper.readValue(
                    interview.getConfiguration() != null ? interview.getConfiguration() : "{}", 
                    Map.class
                );
                int numberOfQuestions = ((Number) config.getOrDefault("numberOfQuestions", 5)).intValue();
                return generateFallbackQuestions(interview, numberOfQuestions);
            } catch (Exception fallbackErr) {
                log.error("Failed to generate fallback questions for session {}: {}", sessionId, fallbackErr.getMessage());
                throw new BusinessException("Failed to generate interview questions");
            }
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

        if (request.getScore() != null) {
            double clamped = Math.max(0.0, Math.min(100.0, request.getScore()));
            answer.setScore(clamped);
            answer.setRating(ratingFromScore(clamped));
            if (request.getFeedback() != null) {
                answer.setFeedback(request.getFeedback());
            }
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
            
            // Parse evaluation result (extract JSON object from AI prose if needed)
            Map<String, Object> evaluation = objectMapper.readValue(extractJsonObject(evaluationJson), Map.class);
            
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
            log.warn("Error evaluating answer for question {}: {}. Keeping existing score or marking UNEVALUATED.", questionOrder, e.getMessage());
            // Do NOT fabricate a fake score — keep existing or set 0 / UNEVALUATED
            if (answer.getScore() == null) {
                answer.setScore(0.0);
                answer.setRating("UNEVALUATED");
            }
            if (answer.getFeedback() == null) {
                answer.setFeedback("Evaluation unavailable. Please retry later.");
            }
            InterviewAnswer savedAnswer = answerRepository.save(answer);
            return interviewMapper.toAnswerResponse(savedAnswer);
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

    private String buildInterviewContext(
            Interview interview,
            Map<String, Object> config,
            String resumeContent
    ) {
        StringBuilder context = new StringBuilder();
        context.append("Target role: ").append(interview.getRole()).append("\n");
        context.append("Target company: ")
                .append(hasText(interview.getCompany()) ? interview.getCompany() : "Not specified")
                .append("\n");
        context.append("Interview round: ").append(interview.getInterviewType().name()).append("\n");
        context.append("Difficulty: ").append(interview.getDifficultyLevel().name()).append("\n");

        appendContextValue(context, "Job description", interview.getJobDescription());
        appendContextValue(context, "Focus areas", config.get("focusAreas"));
        appendContextValue(context, "Custom instructions", config.get("customInstructions"));

        if (hasText(resumeContent)) {
            context.append("\nCandidate resume:\n").append(resumeContent.trim()).append("\n");
        } else {
            context.append("\nCandidate resume: Not provided\n");
        }

        return context.toString();
    }

    private void appendContextValue(StringBuilder context, String label, Object value) {
        if (value != null && hasText(value.toString())) {
            context.append(label).append(": ").append(value.toString().trim()).append("\n");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    String extractJsonObject(String response) {
        if (!hasText(response)) {
            throw new BusinessException("AI returned an empty response");
        }

        int start = response.indexOf('{');
        int end = response.lastIndexOf('}');
        if (start < 0 || end < start) {
            throw new BusinessException("AI response did not contain valid JSON");
        }
        return response.substring(start, end + 1);
    }

    private String ratingFromScore(double score) {
        if (score >= 90) return "EXCELLENT";
        if (score >= 80) return "VERY_GOOD";
        if (score >= 70) return "GOOD";
        if (score >= 60) return "SATISFACTORY";
        if (score >= 50) return "NEEDS_IMPROVEMENT";
        return "POOR";
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

    private List<InterviewQuestionResponse> generateFallbackQuestions(Interview interview, int numberOfQuestions) {
        log.info("Generating fallback questions for interview session {}", interview.getSessionId());
        String role = hasText(interview.getRole()) ? interview.getRole() : "Software Engineer";
        List<InterviewQuestion> questions = new ArrayList<>();
        
        for (int i = 1; i <= Math.max(numberOfQuestions, 1); i++) {
            InterviewQuestion q = new InterviewQuestion();
            q.setInterview(interview);
            q.setQuestionOrder(i);
            q.setDifficultyLevel(interview.getDifficultyLevel());
            q.setExpectedTimeMinutes(5);
            q.setAiGenerated(false);

            if (i == 1) {
                q.setQuestionText(String.format("Tell me about your background and key experiences relevant to the %s role.", role));
                q.setCategory("behavioral");
                q.setEvaluationCriteria(List.of("Clarity of background", "Relevance of experience", "Communication skills"));
                q.setFollowUpQuestions(List.of("What is your biggest technical achievement?", "Why are you interested in this role?"));
            } else if (i == 2) {
                q.setQuestionText(String.format("What key technical concepts, tools, and best practices do you rely on for a %s position?", role));
                q.setCategory("technical");
                q.setEvaluationCriteria(List.of("Technical depth", "Tool proficiency", "Problem-solving methodology"));
                q.setFollowUpQuestions(List.of("Can you walk through a project where you applied these practices?", "How do you stay updated with industry trends?"));
            } else if (i == 3) {
                q.setQuestionText("Describe a challenging technical problem you encountered in a recent project and how you resolved it.");
                q.setCategory("situational");
                q.setEvaluationCriteria(List.of("Analytical thinking", "Troubleshooting skill", "Resourcefulness"));
                q.setFollowUpQuestions(List.of("What trade-offs did you consider?", "What would you do differently next time?"));
            } else if (i == 4) {
                q.setQuestionText("How do you ensure high quality, performance, and security in your code and architectural designs?");
                q.setCategory("technical");
                q.setEvaluationCriteria(List.of("Testing strategy", "Performance tuning", "Security awareness"));
                q.setFollowUpQuestions(List.of("How do you handle technical debt?", "What automated tools do you use for quality assurance?"));
            } else {
                q.setQuestionText(String.format("Question %d: How do you prioritize tasks and collaborate with cross-functional team members under tight deadlines?", i));
                q.setCategory("behavioral");
                q.setEvaluationCriteria(List.of("Collaboration", "Time management", "Prioritization strategy"));
                q.setFollowUpQuestions(List.of("How do you manage scope changes or conflicting priorities?"));
            }
            questions.add(q);
        }
        
        List<InterviewQuestion> saved = questionRepository.saveAll(questions);
        return interviewMapper.toQuestionResponses(saved);
    }
}
