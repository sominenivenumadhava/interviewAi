package com.interviai.backend.module.interview.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviai.backend.common.dto.PageRequest;
import com.interviai.backend.common.dto.PageResponse;
import com.interviai.backend.common.exception.ResourceNotFoundException;
import com.interviai.backend.common.exception.BusinessException;
import com.interviai.backend.module.ai.service.AIService;
import com.interviai.backend.module.interview.dto.request.CreateInterviewRequest;
import com.interviai.backend.module.interview.dto.request.UpdateInterviewStatusRequest;
import com.interviai.backend.module.interview.dto.response.InterviewResponse;
import com.interviai.backend.module.interview.dto.response.InterviewSessionResponse;
import com.interviai.backend.module.interview.entity.Interview;
import com.interviai.backend.module.interview.entity.InterviewQuestion;
import com.interviai.backend.module.interview.enums.InterviewStatus;
import com.interviai.backend.module.interview.mapper.InterviewMapper;
import com.interviai.backend.module.interview.repository.InterviewRepository;
import com.interviai.backend.module.interview.repository.InterviewQuestionRepository;
import com.interviai.backend.module.interview.repository.InterviewAnswerRepository;
import com.interviai.backend.module.interview.service.InterviewService;
import com.interviai.backend.module.interview.service.InterviewQuestionService;
import com.interviai.backend.module.notification.service.EmailService;
import com.interviai.backend.module.resume.entity.Resume;
import com.interviai.backend.module.resume.repository.ResumeRepository;
import com.interviai.backend.module.user.entity.User;
import com.interviai.backend.module.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class InterviewServiceImpl implements InterviewService {
    
    private static final Logger log = LoggerFactory.getLogger(InterviewServiceImpl.class);
    
    @Autowired
    private InterviewRepository interviewRepository;
    
    @Autowired
    private InterviewQuestionRepository questionRepository;
    
    @Autowired
    private InterviewAnswerRepository answerRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ResumeRepository resumeRepository;
    
    @Autowired
    private InterviewQuestionService questionService;
    
    @Autowired
    private InterviewMapper interviewMapper;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private AIService aiService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    public InterviewResponse createInterview(CreateInterviewRequest request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Interview interview = new Interview();
        interview.setUser(user);
        interview.setSessionId(UUID.randomUUID().toString());
        interview.setTitle(generateInterviewTitle(request));
        interview.setCompany(request.getCompany());
        interview.setRole(request.getRole());
        interview.setJobDescription(request.getJobDescription());
        interview.setInterviewType(request.getInterviewType());
        interview.setDifficultyLevel(request.getDifficultyLevel());
        interview.setStatus(InterviewStatus.SCHEDULED);
        interview.setScheduledAt(request.getScheduledAt() != null ? request.getScheduledAt() : LocalDateTime.now());
        interview.setDurationMinutes(request.getDurationMinutes());
        
        if (request.getResumeId() != null) {
            Resume resume = resumeRepository.findById(request.getResumeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
            if (!resume.getUser().getId().equals(userId)) {
                throw new BusinessException("Resume does not belong to the user");
            }
            interview.setResume(resume);
        }
        
        // Store configuration
        interview.setConfiguration(createConfigurationJson(request));
        
        Interview savedInterview = interviewRepository.save(interview);
        
        // Send interview scheduled email
        String interviewDetails = String.format(
            "Role: %s\nCompany: %s\nType: %s\nDifficulty: %s\nDuration: %d minutes\nScheduled: %s",
            savedInterview.getRole(),
            savedInterview.getCompany() != null ? savedInterview.getCompany() : "Not specified",
            savedInterview.getInterviewType().getDisplayName(),
            savedInterview.getDifficultyLevel().getDisplayName(),
            savedInterview.getDurationMinutes(),
            savedInterview.getScheduledAt() != null ? savedInterview.getScheduledAt().toString() : "Start anytime"
        );
        emailService.sendInterviewScheduledEmail(user.getEmail(), interviewDetails);
        
        // Generate questions asynchronously
        if (request.getNumberOfQuestions() > 0) {
            questionService.generateQuestions(savedInterview.getSessionId(), userId);
        }
        
        return interviewMapper.toResponse(savedInterview);
    }
    
    @Override
    public InterviewSessionResponse startInterview(String sessionId, UUID userId) {
        Interview interview = findInterviewBySessionIdAndUser(sessionId, userId);
        
        if (interview.getStatus() != InterviewStatus.SCHEDULED && interview.getStatus() != InterviewStatus.PAUSED) {
            throw new BusinessException("Interview cannot be started in current status: " + interview.getStatus());
        }
        
        interview.start();
        Interview savedInterview = interviewRepository.save(interview);
        
        return createSessionResponse(savedInterview);
    }
    
    @Override
    public InterviewResponse updateInterviewStatus(UpdateInterviewStatusRequest request, UUID userId) {
        Interview interview = findInterviewBySessionIdAndUser(request.getSessionId(), userId);
        
        validateStatusTransition(interview.getStatus(), request.getStatus());
        
        interview.setStatus(request.getStatus());
        
        switch (request.getStatus()) {
            case IN_PROGRESS:
                interview.start();
                break;
            case COMPLETED:
                interview.complete();
                break;
            case PAUSED:
                interview.pause();
                break;
            case CANCELLED:
                interview.cancel();
                break;
        }
        
        Interview savedInterview = interviewRepository.save(interview);
        return interviewMapper.toResponse(savedInterview);
    }
    
    @Override
    @Transactional(readOnly = true)
    public InterviewResponse getInterview(UUID id, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Interview interview = interviewRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found"));
        
        return interviewMapper.toResponse(interview);
    }
    
    @Override
    @Transactional(readOnly = true)
    public InterviewSessionResponse getInterviewSession(String sessionId, UUID userId) {
        Interview interview = findInterviewBySessionIdAndUser(sessionId, userId);
        interview = interviewRepository.findBySessionIdWithQuestionsAndAnswers(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview session not found"));
        
        return createSessionResponse(interview);
    }
    
    @Override
    @Transactional(readOnly = true)
    public PageResponse<InterviewResponse> getUserInterviews(UUID userId, PageRequest pageRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Pageable pageable = pageRequest.toPageable();
        Page<Interview> interviewPage = interviewRepository.findByUser(user, pageable);
        
        List<InterviewResponse> responses = interviewPage.getContent().stream()
                .map(interviewMapper::toResponse)
                .collect(Collectors.toList());
        
        return PageResponse.<InterviewResponse>builder()
                .content(responses)
                .page(interviewPage.getNumber())
                .totalPages(interviewPage.getTotalPages())
                .totalElements(interviewPage.getTotalElements())
                .size(interviewPage.getSize())
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InterviewResponse> getUpcomingInterviews(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        List<Interview> upcomingInterviews = interviewRepository.findUpcomingInterviews(user, InterviewStatus.SCHEDULED);
        
        return upcomingInterviews.stream()
                .map(interviewMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<InterviewResponse> getRecentInterviews(UUID userId, int limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(1);
        
        List<Interview> recentInterviews = interviewRepository.findCompletedInterviewsBetween(user, startDate, endDate);
        
        return recentInterviews.stream()
                .limit(limit)
                .map(interviewMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public void deleteInterview(UUID id, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Interview interview = interviewRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found"));
        
        if (interview.getStatus() == InterviewStatus.IN_PROGRESS) {
            throw new BusinessException("Cannot delete an interview in progress");
        }
        
        interviewRepository.delete(interview);
    }
    
    @Override
    public InterviewResponse completeInterview(String sessionId, UUID userId) {
        Interview interview = findInterviewBySessionIdAndUser(sessionId, userId);
        
        if (interview.getStatus() != InterviewStatus.IN_PROGRESS) {
            throw new BusinessException("Interview must be in progress to complete");
        }

        // Ensure answers are evaluated before averaging
        try {
            questionService.evaluateAllAnswers(sessionId, userId);
        } catch (Exception e) {
            log.warn("evaluateAllAnswers failed for session {}: {}", sessionId, e.getMessage());
        }
        
        // Calculate overall score (default 0.0 when none available)
        Double averageScore = answerRepository.getAverageScoreByInterview(interview);
        interview.setOverallScore(averageScore != null ? averageScore : 0.0);
        
        // Generate overall feedback
        generateOverallFeedback(interview);
        
        interview.complete();
        Interview savedInterview = interviewRepository.save(interview);
        
        // Send completion email
        String interviewSummary = String.format(
            "Interview Details:\n" +
            "Role: %s\n" +
            "Company: %s\n" +
            "Type: %s\n" +
            "Duration: %d minutes\n" +
            "Score: %.1f%%\n" +
            "Performance: %s\n\n" +
            "View detailed results and feedback in your dashboard.",
            savedInterview.getRole(),
            savedInterview.getCompany() != null ? savedInterview.getCompany() : "Not specified",
            savedInterview.getInterviewType().getDisplayName(),
            savedInterview.getActualDuration() != null ? savedInterview.getActualDuration().toMinutes() : savedInterview.getDurationMinutes(),
            savedInterview.getOverallScore() != null ? savedInterview.getOverallScore() : 0.0,
            getPerformanceLevel(savedInterview.getOverallScore())
        );
        emailService.sendInterviewCompletionEmail(savedInterview.getUser().getEmail(), interviewSummary);
        
        return interviewMapper.toResponse(savedInterview);
    }
    
    @Override
    public InterviewResponse pauseInterview(String sessionId, UUID userId) {
        Interview interview = findInterviewBySessionIdAndUser(sessionId, userId);
        interview.pause();
        Interview savedInterview = interviewRepository.save(interview);
        return interviewMapper.toResponse(savedInterview);
    }
    
    @Override
    public InterviewResponse resumeInterview(String sessionId, UUID userId) {
        Interview interview = findInterviewBySessionIdAndUser(sessionId, userId);
        interview.resume();
        Interview savedInterview = interviewRepository.save(interview);
        return interviewMapper.toResponse(savedInterview);
    }
    
    @Override
    public InterviewResponse cancelInterview(String sessionId, UUID userId) {
        Interview interview = findInterviewBySessionIdAndUser(sessionId, userId);
        interview.cancel();
        Interview savedInterview = interviewRepository.save(interview);
        return interviewMapper.toResponse(savedInterview);
    }
    
    // Helper methods
    private Interview findInterviewBySessionIdAndUser(String sessionId, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return interviewRepository.findBySessionIdAndUser(sessionId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found"));
    }
    
    private String generateInterviewTitle(CreateInterviewRequest request) {
        return request.getRole() + " - " + request.getInterviewType().getDisplayName() + 
               (request.getCompany() != null ? " at " + request.getCompany() : "");
    }
    
    private String createConfigurationJson(CreateInterviewRequest request) {
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("numberOfQuestions", request.getNumberOfQuestions());
            config.put("includeFollowUpQuestions", request.getIncludeFollowUpQuestions());
            config.put("includeCodingQuestions", request.getIncludeCodingQuestions());
            config.put("focusAreas", request.getFocusAreas());
            config.put("customInstructions", request.getCustomInstructions());
            config.put("durationMinutes", request.getDurationMinutes());
            return objectMapper.writeValueAsString(config);
        } catch (Exception e) {
            log.error("Error serializing interview configuration: {}", e.getMessage());
            return "{\"numberOfQuestions\":10}";
        }
    }
    
    private InterviewSessionResponse createSessionResponse(Interview interview) {
        InterviewSessionResponse response = new InterviewSessionResponse();
        response.setInterview(interviewMapper.toResponse(interview));
        response.setQuestions(interviewMapper.toQuestionResponses(interview.getQuestions()));
        response.setProgress(createProgressResponse(interview));
        return response;
    }
    
    private InterviewSessionResponse.InterviewProgressResponse createProgressResponse(Interview interview) {
        InterviewSessionResponse.InterviewProgressResponse progress = new InterviewSessionResponse.InterviewProgressResponse();
        
        long totalQuestions = questionRepository.countByInterview(interview);
        long answeredQuestions = questionRepository.countAnsweredQuestions(interview);
        
        progress.setTotalQuestions((int) totalQuestions);
        progress.setAnsweredQuestions((int) answeredQuestions);
        progress.setCompletionPercentage(totalQuestions > 0 ? (answeredQuestions * 100.0) / totalQuestions : 0);
        
        if (interview.getStartedAt() != null) {
            Duration elapsed = Duration.between(interview.getStartedAt(), LocalDateTime.now());
            progress.setElapsedTimeSeconds(elapsed.getSeconds());
            
            if (interview.getDurationMinutes() != null) {
                long totalSeconds = interview.getDurationMinutes() * 60L;
                progress.setRemainingTimeSeconds(Math.max(0, totalSeconds - elapsed.getSeconds()));
            }
        }
        
        return progress;
    }
    
    private void validateStatusTransition(InterviewStatus currentStatus, InterviewStatus newStatus) {
        // Implement status transition validation logic
        if (currentStatus == InterviewStatus.COMPLETED || currentStatus == InterviewStatus.CANCELLED) {
            throw new BusinessException("Cannot change status of a " + currentStatus.getDisplayName() + " interview");
        }
    }
    
    private void generateOverallFeedback(Interview interview) {
        try {
            // Build summary of the interview performance
            Double avgScore = answerRepository.getAverageScoreByInterview(interview);
            String scoreText = avgScore != null ? String.format("%.1f%%", avgScore) : "N/A";
            
            String prompt = String.format(
                "Generate a comprehensive interview feedback summary for a %s interview at %s level. " +
                "Role: %s. Overall Score: %s. " +
                "Provide: 1) Overall feedback paragraph, 2) Top 3 strengths, 3) Top 3 improvement areas. " +
                "Be concise and constructive. Format as JSON with keys: overallFeedback, strengths (array), improvements (array).",
                interview.getInterviewType().getDisplayName(),
                interview.getDifficultyLevel().getDisplayName(),
                interview.getRole(),
                scoreText
            );
            
            String aiResponse = aiService.generateContent(prompt).block();
            if (aiResponse != null) {
                try {
                    // Try to parse structured response
                    Map<String, Object> parsed = objectMapper.readValue(
                        aiResponse.contains("{") ? aiResponse.substring(aiResponse.indexOf("{"), aiResponse.lastIndexOf("}") + 1) : "{}",
                        Map.class
                    );
                    interview.setOverallFeedback((String) parsed.getOrDefault("overallFeedback", aiResponse));
                    if (parsed.get("strengths") instanceof List<?> strengths) {
                        interview.setStrengths(String.join(", ", (List<String>) strengths));
                    }
                    if (parsed.get("improvements") instanceof List<?> improvements) {
                        interview.setImprovements(String.join(", ", (List<String>) improvements));
                    }
                } catch (Exception parseEx) {
                    // Fallback: store raw AI response as feedback
                    interview.setOverallFeedback(aiResponse);
                }
            } else {
                interview.setOverallFeedback(buildFallbackFeedback(interview));
            }
        } catch (Exception e) {
            log.error("Error generating AI feedback for interview {}: {}", interview.getSessionId(), e.getMessage());
            interview.setOverallFeedback(buildFallbackFeedback(interview));
        }
    }
    
    private String buildFallbackFeedback(Interview interview) {
        Double score = interview.getOverallScore();
        String level = getPerformanceLevel(score);
        return String.format("Interview completed. Overall performance: %s (Score: %.1f%%). " +
            "Review your answers in the evaluation section for detailed feedback.",
            level, score != null ? score : 0.0);
    }
    
    private String getPerformanceLevel(Double score) {
        if (score == null) return "Not evaluated";
        if (score >= 90) return "Excellent";
        if (score >= 80) return "Very Good";
        if (score >= 70) return "Good";
        if (score >= 60) return "Satisfactory";
        return "Needs Improvement";
    }
}
