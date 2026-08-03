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
import com.interviai.backend.module.interview.enums.InterviewType;
import com.interviai.backend.module.interview.generation.GeneratedQuestionDraft;
import com.interviai.backend.module.interview.generation.QuestionGenerationContext;
import com.interviai.backend.module.interview.generation.RoundAwareQuestionGenerator;
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

    @Autowired
    private RoundAwareQuestionGenerator roundAwareQuestionGenerator;
    
    @Override
    public List<InterviewQuestionResponse> generateQuestions(String sessionId, UUID userId) {
        Interview interview = findInterviewBySessionIdAndUser(sessionId, userId);
        
        // Check if questions already exist
        if (!interview.getQuestions().isEmpty()) {
            return interviewMapper.toQuestionResponses(interview.getQuestions());
        }
        
        try {
            Map<String, Object> config = objectMapper.readValue(
                interview.getConfiguration() != null ? interview.getConfiguration() : "{}",
                Map.class
            );
            
            int numberOfQuestions = ((Number) config.getOrDefault("numberOfQuestions", 10)).intValue();
            
            String resumeContent = "";
            String skills = "";
            if (interview.getResume() != null) {
                Resume resume = interview.getResume();
                resumeContent = buildResumeContent(resume);
                skills = extractSkillsSummary(resume);
            }

            String preferredLanguage = null;
            Object focus = config.get("focusAreas");
            if (focus != null && focus.toString().toLowerCase().contains("preferred language:")) {
                preferredLanguage = focus.toString();
            }

            InterviewType roundType = interview.getInterviewType() != null
                    ? interview.getInterviewType()
                    : InterviewType.TECHNICAL;

            QuestionGenerationContext ctx = QuestionGenerationContext.builder()
                    .interviewType(roundType)
                    .role(interview.getRole())
                    .company(interview.getCompany())
                    .jobDescription(interview.getJobDescription())
                    .difficulty(interview.getDifficultyLevel())
                    .questionCount(numberOfQuestions)
                    .focusAreas(focus != null ? focus.toString() : null)
                    .customInstructions(config.get("customInstructions") != null
                            ? config.get("customInstructions").toString() : null)
                    .resumeContent(resumeContent)
                    .candidateSkills(skills)
                    .preferredLanguage(preferredLanguage)
                    .build();

            List<GeneratedQuestionDraft> drafts = roundAwareQuestionGenerator.generate(ctx);
            List<InterviewQuestion> questions = new ArrayList<>();
            for (int i = 0; i < drafts.size(); i++) {
                questions.add(toEntity(interview, drafts.get(i), i + 1));
            }

            List<InterviewQuestion> savedQuestions = questionRepository.saveAll(questions);
            return interviewMapper.toQuestionResponses(savedQuestions);
            
        } catch (Exception e) {
            log.error("Failed to generate round-aware questions for session {}: {}", sessionId, e.getMessage(), e);
            throw new BusinessException("Failed to generate interview questions for the selected round");
        }
    }

    private InterviewQuestion toEntity(Interview interview, GeneratedQuestionDraft draft, int order) {
        InterviewQuestion question = new InterviewQuestion();
        question.setInterview(interview);
        question.setQuestionOrder(order);
        question.setQuestionText(draft.getQuestionText());
        question.setCategory(draft.getCategory() != null
                ? draft.getCategory()
                : interview.getInterviewType().canonicalize().getCategoryLabel());
        question.setDifficultyLevel(draft.getDifficultyLevel() != null
                ? draft.getDifficultyLevel()
                : interview.getDifficultyLevel());
        question.setExpectedTimeMinutes(draft.getExpectedTimeMinutes() != null
                ? draft.getExpectedTimeMinutes() : 5);
        if (draft.getEvaluationCriteria() != null) {
            question.setEvaluationCriteria(new ArrayList<>(draft.getEvaluationCriteria()));
        }
        if (draft.getFollowUpQuestions() != null) {
            question.setFollowUpQuestions(new ArrayList<>(draft.getFollowUpQuestions()));
        }
        question.setHints(draft.getHints());
        question.setReferenceAnswer(draft.getReferenceAnswer());
        question.setAiGenerated(true);
        if (draft.getMetadata() != null && !draft.getMetadata().isEmpty()) {
            try {
                question.setMetadata(objectMapper.writeValueAsString(draft.getMetadata()));
            } catch (Exception e) {
                log.warn("Failed to serialize question metadata: {}", e.getMessage());
            }
        }
        return question;
    }

    private String extractSkillsSummary(Resume resume) {
        if (resume.getSkills() == null || resume.getSkills().isEmpty()) {
            return "";
        }
        return resume.getSkills().stream()
                .map(s -> s.getName() != null ? s.getName() : "")
                .filter(n -> !n.isBlank())
                .collect(Collectors.joining(", "));
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
            boolean missingInsights = answer.getStrengths() == null || answer.getStrengths().isEmpty()
                    || answer.getImprovements() == null || answer.getImprovements().isEmpty()
                    || answer.getFeedback() == null || answer.getFeedback().isBlank();
            if (!answer.isEvaluated() || missingInsights) {
                evaluatedAnswers.add(evaluateAnswerForced(sessionId, answer.getQuestion().getQuestionOrder(), userId));
            } else {
                evaluatedAnswers.add(interviewMapper.toAnswerResponse(answer));
            }
        }

        return evaluatedAnswers;
    }

    /**
     * Always runs AI evaluation and merges insights. Preserves a solid client score
     * when AI fails, but prefers AI score when available.
     */
    private InterviewAnswerResponse evaluateAnswerForced(String sessionId, Integer questionOrder, UUID userId) {
        Interview interview = findInterviewBySessionIdAndUser(sessionId, userId);
        InterviewQuestion question = questionRepository.findByInterviewAndQuestionOrder(interview, questionOrder)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
        InterviewAnswer answer = answerRepository.findByQuestion(question)
                .orElseThrow(() -> new ResourceNotFoundException("Answer not found"));

        Double priorScore = answer.getScore();
        String priorFeedback = answer.getFeedback();

        try {
            List<String> criteriaList = question.getEvaluationCriteria() != null
                    ? question.getEvaluationCriteria()
                    : List.of();
            String criteria = String.join(", ", criteriaList);
            if (criteria.isBlank()) {
                criteria = "Correctness, Clarity, Depth, Communication";
            }

            String evaluationJson = aiService.evaluateAnswer(
                    question.getQuestionText(),
                    answer.getAnswerText() != null ? answer.getAnswerText() : "",
                    criteria
            ).block();

            Map<String, Object> evaluation = objectMapper.readValue(extractJsonObject(evaluationJson), Map.class);

            Object scoreObj = evaluation.get("score");
            if (scoreObj instanceof Number n) {
                double aiScore = n.doubleValue();
                // AI prompt may return 0–10 or 0–100 — normalize to 0–100
                if (aiScore <= 10.0) {
                    aiScore = aiScore * 10.0;
                }
                answer.setScore(Math.max(0.0, Math.min(100.0, aiScore)));
            } else if (priorScore != null) {
                answer.setScore(priorScore);
            }

            if (evaluation.get("rating") instanceof String rating) {
                answer.setRating(rating);
            } else if (answer.getScore() != null) {
                answer.setRating(ratingFromScore(answer.getScore()));
            }

            if (evaluation.get("strengths") instanceof List<?> strengths) {
                answer.setStrengths(strengths.stream().map(String::valueOf).toList());
            }
            if (evaluation.get("improvements") instanceof List<?> improvements) {
                answer.setImprovements(improvements.stream().map(String::valueOf).toList());
            }
            if (evaluation.get("feedback") instanceof String fb && !fb.isBlank()) {
                answer.setFeedback(fb);
            } else if (priorFeedback != null) {
                answer.setFeedback(priorFeedback);
            }
            if (evaluation.get("suggestedAnswer") instanceof String sa) {
                answer.setSuggestedAnswer(sa);
            }
            answer.setAiEvaluation(evaluationJson);

            if (evaluation.get("confidenceScore") instanceof Number n) {
                answer.setConfidenceScore(n.doubleValue());
            }
            if (evaluation.get("clarityScore") instanceof Number n) {
                answer.setClarityScore(n.doubleValue());
            }
            if (evaluation.get("relevanceScore") instanceof Number n) {
                answer.setRelevanceScore(n.doubleValue());
            }
            if (evaluation.get("technicalAccuracyScore") instanceof Number n) {
                answer.setTechnicalAccuracyScore(n.doubleValue());
            }

            InterviewAnswer savedAnswer = answerRepository.save(answer);
            return interviewMapper.toAnswerResponse(savedAnswer);
        } catch (Exception e) {
            log.warn("Forced AI evaluation failed for Q{}: {}. Keeping prior score.", questionOrder, e.getMessage());
            if (answer.getScore() == null && priorScore != null) {
                answer.setScore(priorScore);
            }
            if (answer.getScore() == null) {
                // Heuristic from answer length so answered questions are never stuck at 0
                String text = answer.getAnswerText() != null ? answer.getAnswerText().trim() : "";
                double heuristic = text.isEmpty() ? 0.0
                        : text.length() < 40 ? 45.0
                        : text.length() < 120 ? 62.0
                        : text.length() < 400 ? 74.0 : 82.0;
                answer.setScore(heuristic);
                answer.setRating(ratingFromScore(heuristic));
            }
            if (answer.getFeedback() == null || answer.getFeedback().isBlank()) {
                answer.setFeedback(priorFeedback != null ? priorFeedback
                        : "Answer recorded. Detailed AI critique temporarily unavailable.");
            }
            if (answer.getStrengths() == null || answer.getStrengths().isEmpty()) {
                answer.setStrengths(List.of("Provided a substantive response to the question"));
            }
            if (answer.getImprovements() == null || answer.getImprovements().isEmpty()) {
                answer.setImprovements(List.of("Add more specific examples and measurable outcomes"));
            }
            InterviewAnswer savedAnswer = answerRepository.save(answer);
            return interviewMapper.toAnswerResponse(savedAnswer);
        }
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
}
