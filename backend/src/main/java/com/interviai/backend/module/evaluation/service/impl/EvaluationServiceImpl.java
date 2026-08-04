package com.interviai.backend.module.evaluation.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import com.interviai.backend.common.exception.ResourceNotFoundException;
import java.util.UUID;
import com.interviai.backend.module.ai.service.AIService;
import java.util.UUID;
import com.interviai.backend.module.evaluation.dto.InterviewEvaluationResponse;
import java.util.UUID;
import com.interviai.backend.module.evaluation.dto.SkillGapAnalysisResponse;
import java.util.UUID;
import com.interviai.backend.module.evaluation.service.EvaluationService;
import java.util.UUID;
import com.interviai.backend.module.interview.entity.Interview;
import java.util.UUID;
import com.interviai.backend.module.interview.entity.InterviewAnswer;
import java.util.UUID;
import com.interviai.backend.module.interview.entity.InterviewQuestion;
import java.util.UUID;
import com.interviai.backend.module.interview.enums.InterviewStatus;
import java.util.UUID;
import com.interviai.backend.module.interview.repository.InterviewAnswerRepository;
import java.util.UUID;
import com.interviai.backend.module.interview.repository.InterviewRepository;
import java.util.UUID;
import com.interviai.backend.module.user.entity.User;
import java.util.UUID;
import com.interviai.backend.module.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import com.interviai.backend.module.resume.entity.Resume;
import java.util.UUID;
import com.interviai.backend.module.resume.entity.Skill;
import java.util.UUID;
import com.interviai.backend.module.resume.repository.ResumeRepository;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class EvaluationServiceImpl implements EvaluationService {
    
    private static final Logger log = LoggerFactory.getLogger(EvaluationServiceImpl.class);
    
    @Autowired
    private InterviewRepository interviewRepository;
    
    @Autowired
    private InterviewAnswerRepository answerRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AIService aiService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ResumeRepository resumeRepository;
    
    @Override
    @Transactional(readOnly = true)
    public InterviewEvaluationResponse generateInterviewEvaluation(String sessionId, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Interview interview = interviewRepository.findBySessionIdWithQuestionsAndAnswers(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview not found"));

        if (!interview.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Interview not found for user");
        }

        // Allow report for completed interviews; also tolerate just-finished sessions
        if (interview.getStatus() != InterviewStatus.COMPLETED
                && interview.getStatus() != InterviewStatus.IN_PROGRESS) {
            throw new IllegalStateException("Interview must be completed for evaluation");
        }

        InterviewEvaluationResponse response = new InterviewEvaluationResponse();
        response.setInterviewId(interview.getId());
        response.setSessionId(interview.getSessionId());
        response.setRole(interview.getRole());
        response.setCompany(interview.getCompany());
        response.setInterviewType(
                interview.getInterviewType() != null ? interview.getInterviewType().name() : null);
        response.setDifficultyLevel(interview.getDifficultyLevel());
        response.setCompletedAt(interview.getCompletedAt());
        response.setTotalQuestions(interview.getQuestions() != null ? interview.getQuestions().size() : 0);

        long answered = interview.getAnswers() == null ? 0
                : interview.getAnswers().stream().filter(InterviewAnswer::isSubmitted).count();
        response.setAnsweredQuestions((int) answered);

        calculateOverallScores(interview, response);
        analyzeQuestionPerformance(interview, response);
        generateStrengthsAndImprovements(interview, response);
        calculateCategoryScores(interview, response);
        performSkillsAssessment(interview, response);

        // AI holistic report — fills dimensions, strengths, recommendations from real Q&A
        applyHolisticAiReport(interview, response);

        generateBenchmarkComparison(interview, response);
        generateDetailedFeedback(interview, response);

        // Guard: answered > 0 ⇒ overall must not stay at 0 if answers have content
        if (response.getAnsweredQuestions() != null
                && response.getAnsweredQuestions() > 0
                && (response.getOverallScore() == null || response.getOverallScore() <= 0)) {
            double recovered = recoverScoreFromAnswers(interview);
            response.setOverallScore(recovered);
            response.setOverallRating(determineRating(recovered));
            response.setPerformanceLevel(determinePerformanceLevel(recovered));
        }

        if (response.getHiringProbability() == null && response.getOverallScore() != null) {
            response.setHiringProbability(
                    (double) Math.min(97, Math.max(8, Math.round(response.getOverallScore() * 0.85 + 10))));
        }

        return response;
    }

    private double recoverScoreFromAnswers(Interview interview) {
        List<InterviewAnswer> scored = interview.getAnswers().stream()
                .filter(a -> a.isSubmitted() && a.getScore() != null && a.getScore() > 0)
                .toList();
        if (!scored.isEmpty()) {
            return scored.stream().mapToDouble(InterviewAnswer::getScore).average().orElse(55.0);
        }
        // Content-based heuristic
        return interview.getAnswers().stream()
                .filter(InterviewAnswer::isSubmitted)
                .mapToDouble(a -> {
                    String t = a.getAnswerText() != null ? a.getAnswerText().trim() : "";
                    if (t.isEmpty()) return 0;
                    if (t.length() < 40) return 48;
                    if (t.length() < 120) return 64;
                    if (t.length() < 400) return 76;
                    return 84;
                })
                .average()
                .orElse(0.0);
    }

    @SuppressWarnings("unchecked")
    private void applyHolisticAiReport(Interview interview, InterviewEvaluationResponse response) {
        List<InterviewAnswer> submitted = interview.getAnswers() == null ? List.of()
                : interview.getAnswers().stream().filter(InterviewAnswer::isSubmitted).toList();
        if (submitted.isEmpty()) {
            response.setOverallScore(0.0);
            response.setTopStrengths(List.of("No answers were submitted for this interview."));
            response.setKeyImprovements(List.of("Complete at least one question to receive a scored report."));
            response.setRecommendedPractice(List.of("Retake the interview and answer each question fully."));
            return;
        }

        try {
            String round = interview.getInterviewType() != null
                    ? interview.getInterviewType().canonicalize().name()
                    : "TECHNICAL";
            String criteriaBlock = switch (round) {
                case "CODING" ->
                        "Correctness, Approach, Time Complexity, Space Complexity, Edge Cases, Optimization, Communication";
                case "SYSTEM_DESIGN" ->
                        "Architecture, Scalability, Database, Caching, Trade-offs, API Design, Communication";
                case "HR", "BEHAVIORAL" ->
                        "Communication, Confidence, Leadership, STAR framework, Professionalism, Cultural Fit";
                case "MANAGERIAL" ->
                        "Decision Making, Ownership, Leadership, Stakeholder Management, Prioritization";
                case "APTITUDE" ->
                        "Accuracy, Logical Reasoning, Speed, Clarity of Approach";
                default ->
                        "Concepts, Accuracy, Technical Depth, Practical Knowledge, Examples, Communication";
            };

            StringBuilder qa = new StringBuilder();
            for (InterviewAnswer a : submitted) {
                InterviewQuestion q = a.getQuestion();
                qa.append("Q").append(q.getQuestionOrder()).append(" [").append(q.getCategory()).append("]\n");
                qa.append("Question: ").append(q.getQuestionText()).append("\n");
                qa.append("Candidate Answer: ")
                        .append(a.getAnswerText() != null && !a.getAnswerText().isBlank()
                                ? a.getAnswerText() : "(empty)")
                        .append("\n");
                if (a.getScore() != null) {
                    qa.append("Existing score (0-100): ").append(a.getScore()).append("\n");
                }
                qa.append("\n");
            }

            String system = """
                You are a senior bar-raiser interviewer writing a final interview scorecard for %s at %s.
                Interview round: %s. Evaluate ONLY from the provided Q&A. Never invent answers the candidate did not give.
                Round-specific criteria: %s.
                
                Return raw JSON only:
                {
                  "overallScore": 0-100,
                  "hiringProbability": 0-100,
                  "communication": 0-100,
                  "technicalDepth": 0-100,
                  "problemSolving": 0-100,
                  "confidence": 0-100,
                  "correctness": 0-100,
                  "optimization": 0-100,
                  "timeManagement": 0-100,
                  "strengths": ["..."],
                  "improvements": ["..."],
                  "recommendedPractice": ["..."],
                  "detailedFeedback": "2-4 sentence summary",
                  "questionFeedback": [
                    {"order": 1, "score": 0-100, "feedback": "...", "strengths": ["..."], "weaknesses": ["..."]}
                  ]
                }
                
                Rules:
                - If the candidate answered with real content, overallScore MUST be > 0.
                - Empty answers score near 0 for that question only.
                - Strengths and improvements must reference actual answer content.
                """.formatted(
                    interview.getRole() != null ? interview.getRole() : "the role",
                    interview.getCompany() != null ? interview.getCompany() : "the company",
                    round,
                    criteriaBlock
            );

            String userPrompt = "Interview Q&A:\n" + qa + "\nProduce the final scorecard JSON now.";
            String raw = aiService.generateStructuredContent(userPrompt, system).block();
            if (raw == null || raw.isBlank()) {
                return;
            }

            int start = raw.indexOf('{');
            int end = raw.lastIndexOf('}');
            if (start < 0 || end <= start) {
                return;
            }
            Map<String, Object> report = objectMapper.readValue(raw.substring(start, end + 1), Map.class);

            Double overall = asDouble(report.get("overallScore"));
            if (overall != null) {
                response.setOverallScore(clamp100(overall));
                response.setOverallRating(determineRating(response.getOverallScore()));
                response.setPerformanceLevel(determinePerformanceLevel(response.getOverallScore()));
            }

            Double hire = asDouble(report.get("hiringProbability"));
            if (hire != null) {
                response.setHiringProbability(clamp100(hire));
            }

            InterviewEvaluationResponse.CategoryScores cats = response.getCategoryScores();
            if (cats == null) {
                cats = new InterviewEvaluationResponse.CategoryScores();
            }
            setDim(cats::setCommunicationScore, report.get("communication"));
            setDim(cats::setTechnicalScore, report.get("technicalDepth"));
            setDim(cats::setProblemSolvingScore, report.get("problemSolving"));
            setDim(cats::setConfidenceScore, report.get("confidence"));
            setDim(cats::setBehavioralScore, report.get("confidence"));
            setDim(cats::setCorrectnessScore, report.get("correctness"));
            setDim(cats::setOptimizationScore, report.get("optimization"));
            setDim(cats::setTimeManagementScore, report.get("timeManagement"));
            setDim(cats::setDomainKnowledgeScore, report.get("technicalDepth"));
            // Fill nulls from overall
            fillNullDims(cats, response.getOverallScore());
            response.setCategoryScores(cats);

            List<String> strengths = asStringList(report.get("strengths"));
            if (!strengths.isEmpty()) {
                response.setTopStrengths(strengths);
            }
            List<String> improvements = asStringList(report.get("improvements"));
            if (!improvements.isEmpty()) {
                response.setKeyImprovements(improvements);
            }
            List<String> practice = asStringList(report.get("recommendedPractice"));
            if (!practice.isEmpty()) {
                response.setRecommendedPractice(practice);
            }
            if (report.get("detailedFeedback") instanceof String df && !df.isBlank()) {
                response.setDetailedFeedback(df);
            }

            // Merge per-question AI feedback
            if (report.get("questionFeedback") instanceof List<?> qfList
                    && response.getQuestionPerformances() != null) {
                Map<Integer, Map<String, Object>> byOrder = new HashMap<>();
                for (Object item : qfList) {
                    if (item instanceof Map<?, ?> m) {
                        Object order = m.get("order");
                        if (order instanceof Number n) {
                            byOrder.put(n.intValue(), (Map<String, Object>) m);
                        }
                    }
                }
                for (InterviewEvaluationResponse.QuestionPerformance perf : response.getQuestionPerformances()) {
                    Map<String, Object> fb = byOrder.get(perf.getQuestionOrder());
                    if (fb == null) continue;
                    Double qs = asDouble(fb.get("score"));
                    if (qs != null) {
                        double s = qs <= 10 ? qs * 10 : qs;
                        perf.setScore(clamp100(s));
                    }
                    if (fb.get("feedback") instanceof String f) {
                        perf.setFeedback(f);
                    }
                    List<String> qsStrengths = asStringList(fb.get("strengths"));
                    if (!qsStrengths.isEmpty()) {
                        perf.setStrengths(qsStrengths);
                    }
                    List<String> qsWeak = asStringList(fb.get("weaknesses"));
                    if (qsWeak.isEmpty()) {
                        qsWeak = asStringList(fb.get("improvements"));
                    }
                    if (!qsWeak.isEmpty()) {
                        perf.setWeaknesses(qsWeak);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Holistic AI evaluation unavailable for {}: {}", interview.getSessionId(), e.getMessage());
        }
    }

    private void setDim(java.util.function.Consumer<Double> setter, Object raw) {
        Double v = asDouble(raw);
        if (v != null) {
            setter.accept(clamp100(v));
        }
    }

    private void fillNullDims(InterviewEvaluationResponse.CategoryScores cats, Double overall) {
        double fallback = overall != null ? overall : 0.0;
        if (cats.getCommunicationScore() == null) cats.setCommunicationScore(fallback);
        if (cats.getTechnicalScore() == null) cats.setTechnicalScore(fallback);
        if (cats.getProblemSolvingScore() == null) cats.setProblemSolvingScore(fallback);
        if (cats.getConfidenceScore() == null) cats.setConfidenceScore(fallback);
        if (cats.getBehavioralScore() == null) cats.setBehavioralScore(fallback);
        if (cats.getCorrectnessScore() == null) cats.setCorrectnessScore(fallback);
        if (cats.getOptimizationScore() == null) cats.setOptimizationScore(fallback);
        if (cats.getTimeManagementScore() == null) cats.setTimeManagementScore(fallback);
        if (cats.getDomainKnowledgeScore() == null) cats.setDomainKnowledgeScore(fallback);
    }

    private Double asDouble(Object raw) {
        if (raw instanceof Number n) return n.doubleValue();
        if (raw instanceof String s) {
            try {
                return Double.parseDouble(s.replace("%", "").trim());
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private List<String> asStringList(Object raw) {
        if (!(raw instanceof List<?> list)) return List.of();
        return list.stream().filter(Objects::nonNull).map(String::valueOf)
                .filter(s -> !s.isBlank()).toList();
    }

    private double clamp100(double v) {
        return Math.max(0.0, Math.min(100.0, v));
    }
    
    @Override
    public SkillGapAnalysisResponse getSkillGapAnalysis(UUID userId, String targetRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        SkillGapAnalysisResponse response = new SkillGapAnalysisResponse();
        response.setUserId(userId);
        response.setTargetRole(targetRole);
        
        try {
            // Get user's current skills from recent interviews
            String userSkills = extractUserSkills(user);
            
            // Get AI-powered skill gap analysis
            String analysisJson = aiService.analyzeSkillGap(userSkills, targetRole).block();
            if (analysisJson == null || analysisJson.isBlank()) {
                throw new IllegalStateException("AI returned empty skill-gap analysis");
            }
            Map<String, Object> analysisData = objectMapper.readValue(analysisJson, Map.class);
            
            // Build response from AI analysis
            buildSkillGapResponse(response, analysisData);
            
            // Add performance trends
            response.setPerformanceTrends(getPerformanceTrends(userId, 10));
            
        } catch (Exception e) {
            log.warn("Skill gap AI analysis unavailable for user {}: {}. Returning empty analysis.", userId, e.getMessage());
            // Honest empty response — do not invent skill gaps
            SkillGapAnalysisResponse.SkillsMatching emptyMatching = new SkillGapAnalysisResponse.SkillsMatching();
            emptyMatching.setMatchedSkills(List.of());
            emptyMatching.setSkillGaps(List.of());
            emptyMatching.setMatchPercentage(0.0);
            emptyMatching.setCategoryMatchPercentages(Map.of());
            response.setSkillsMatching(emptyMatching);
            SkillGapAnalysisResponse.LearningRecommendations emptyRecs = new SkillGapAnalysisResponse.LearningRecommendations();
            emptyRecs.setImmediateActions(List.of());
            emptyRecs.setShortTermGoals(List.of());
            emptyRecs.setLongTermGoals(List.of());
            emptyRecs.setEstimatedWeeksToCloseGap(null);
            response.setLearningRecommendations(emptyRecs);
            response.setLearningRoadmap(List.of());
            response.setOverallReadiness(0.0);
            response.setReadinessLevel("NEEDS_PREPARATION");
            try {
                response.setPerformanceTrends(getPerformanceTrends(userId, 10));
            } catch (Exception trendsEx) {
                log.debug("Performance trends unavailable: {}", trendsEx.getMessage());
            }
        }
        
        return response;
    }
    
    @Override
    @Transactional(readOnly = true)
    public SkillGapAnalysisResponse.PerformanceTrends getPerformanceTrends(UUID userId, int limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        List<Interview> recentInterviews = interviewRepository.findCompletedInterviewsBetween(
                user,
                user.getCreatedAt(),
                user.getUpdatedAt()
        ).stream()
                .filter(i -> i.getOverallScore() != null)
                .sorted((a, b) -> b.getCompletedAt().compareTo(a.getCompletedAt()))
                .limit(limit)
                .collect(Collectors.toList());
        
        SkillGapAnalysisResponse.PerformanceTrends trends = new SkillGapAnalysisResponse.PerformanceTrends();
        
        if (!recentInterviews.isEmpty()) {
            // Calculate average score
            double avgScore = recentInterviews.stream()
                    .mapToDouble(Interview::getOverallScore)
                    .average()
                    .orElse(0.0);
            trends.setAverageScore(avgScore);
            
            // Calculate improvement rate
            if (recentInterviews.size() >= 2) {
                double firstScore = recentInterviews.get(recentInterviews.size() - 1).getOverallScore();
                double lastScore = recentInterviews.get(0).getOverallScore();
                double improvementRate = ((lastScore - firstScore) / firstScore) * 100;
                trends.setImprovementRate(improvementRate);
                trends.setTrend(improvementRate > 5 ? "IMPROVING" : 
                               improvementRate < -5 ? "DECLINING" : "STABLE");
            } else {
                trends.setImprovementRate(0.0);
                trends.setTrend("STABLE");
            }
            
            // Build interview score trends
            List<SkillGapAnalysisResponse.InterviewScoreTrend> scoreTrends = recentInterviews.stream()
                    .map(this::mapToScoreTrend)
                    .collect(Collectors.toList());
            trends.setRecentInterviews(scoreTrends);
            
            // Calculate category trends
            Map<String, Double> categoryTrends = calculateCategoryTrends(recentInterviews);
            trends.setCategoryTrends(categoryTrends);
        } else {
            trends.setAverageScore(0.0);
            trends.setImprovementRate(0.0);
            trends.setTrend("NO_DATA");
            trends.setRecentInterviews(new ArrayList<>());
            trends.setCategoryTrends(new HashMap<>());
        }
        
        return trends;
    }
    
    @Override
    public InterviewEvaluationResponse.BenchmarkComparison getBenchmarkComparison(UUID userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Get user's average score for the role
        Double userAvgScore = interviewRepository.getAverageScoreByUser(user);
        
        InterviewEvaluationResponse.BenchmarkComparison comparison = new InterviewEvaluationResponse.BenchmarkComparison();
        
        // For demonstration, using predefined benchmarks
        // In production, this would come from a benchmark database
        Map<String, Double> roleBenchmarks = getRoleBenchmarks(role);
        Double benchmarkScore = roleBenchmarks.getOrDefault("average", 75.0);
        
        comparison.setAverageScoreForRole(benchmarkScore);
        
        if (userAvgScore != null) {
            // Calculate percentile rank
            double percentile = calculatePercentileRank(userAvgScore, role);
            comparison.setPercentileRank(percentile);
            
            // Generate comparison message
            String message = generateComparisonMessage(userAvgScore, benchmarkScore, percentile);
            comparison.setComparisonMessage(message);
            
            // Category comparisons
            Map<String, Double> categoryComparisons = generateCategoryComparisons(userId, role);
            comparison.setCategoryComparisons(categoryComparisons);
        }
        
        return comparison;
    }
    
    @Override
    public byte[] generateInterviewReport(String sessionId, UUID userId, String format) {
        // This would generate a PDF or HTML report
        // For now, returning a placeholder
        InterviewEvaluationResponse evaluation = generateInterviewEvaluation(sessionId, userId);
        
        try {
            if ("json".equalsIgnoreCase(format)) {
                return objectMapper.writeValueAsBytes(evaluation);
            }
            // In production, use a proper PDF/HTML generation library
            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(evaluation)
                    .getBytes();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate report", e);
        }
    }
    
    @Override
    public List<String> getRecommendedPracticeQuestions(UUID userId, String role, int count) {
        SkillGapAnalysisResponse skillGap = getSkillGapAnalysis(userId, role);
        
        List<String> focusAreas = new ArrayList<>();
        if (skillGap.getSkillsMatching() != null && skillGap.getSkillsMatching().getSkillGaps() != null) {
            focusAreas = skillGap.getSkillsMatching().getSkillGaps().stream()
                    .filter(gap -> "CRITICAL".equals(gap.getImportance()) || "HIGH".equals(gap.getImportance()))
                    .map(SkillGapAnalysisResponse.SkillGap::getSkillName)
                    .limit(5)
                    .collect(Collectors.toList());
        }
        
        // Generate practice questions focusing on skill gaps
        List<String> questions = new ArrayList<>();
        
        // This would typically call AI service to generate questions
        // For now, returning sample questions
        questions.add("Explain the concept of " + (focusAreas.isEmpty() ? "system design" : focusAreas.get(0)) + " and provide a real-world example.");
        questions.add("How would you optimize a " + role + " solution for scalability?");
        questions.add("Describe a challenging project you worked on and how you overcame obstacles.");
        
        return questions.stream().limit(count).collect(Collectors.toList());
    }
    
    // Helper methods
    private void calculateOverallScores(Interview interview, InterviewEvaluationResponse response) {
        Double avgScore = answerRepository.getAverageScoreByInterview(interview);
        response.setOverallScore(avgScore != null ? avgScore : 0.0);
        
        String rating = determineRating(response.getOverallScore());
        response.setOverallRating(rating);
        
        String performanceLevel = determinePerformanceLevel(response.getOverallScore());
        response.setPerformanceLevel(performanceLevel);
    }
    
    private void calculateCategoryScores(Interview interview, InterviewEvaluationResponse response) {
        InterviewEvaluationResponse.CategoryScores scores = new InterviewEvaluationResponse.CategoryScores();

        List<InterviewAnswer> submitted = interview.getAnswers() == null ? List.of()
                : interview.getAnswers().stream()
                .filter(a -> a.isSubmitted() && a.getScore() != null)
                .toList();

        if (submitted.isEmpty()) {
            response.setCategoryScores(scores);
            return;
        }

        double avg = submitted.stream().mapToDouble(InterviewAnswer::getScore).average().orElse(0);
        // Seed all dimensions from answer average; AI holistic report overwrites when available
        scores.setTechnicalScore(avg);
        scores.setCommunicationScore(Math.min(100, avg * 1.02));
        scores.setProblemSolvingScore(avg);
        scores.setBehavioralScore(Math.min(100, avg * 0.98));
        scores.setDomainKnowledgeScore(avg);
        scores.setCorrectnessScore(avg);
        scores.setOptimizationScore(Math.min(100, avg * 0.96));
        scores.setTimeManagementScore(Math.min(100, avg * 0.97));
        scores.setConfidenceScore(Math.min(100, avg * 1.01));

        // Refine from answer sub-scores when present
        submitted.stream()
                .map(InterviewAnswer::getClarityScore)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .ifPresent(v -> scores.setCommunicationScore(clamp100(v <= 10 ? v * 10 : v)));
        submitted.stream()
                .map(InterviewAnswer::getTechnicalAccuracyScore)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .ifPresent(v -> scores.setTechnicalScore(clamp100(v <= 10 ? v * 10 : v)));
        submitted.stream()
                .map(InterviewAnswer::getConfidenceScore)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .ifPresent(v -> scores.setConfidenceScore(clamp100(v <= 10 ? v * 10 : v)));

        response.setCategoryScores(scores);
    }

    private void analyzeQuestionPerformance(Interview interview, InterviewEvaluationResponse response) {
        List<InterviewEvaluationResponse.QuestionPerformance> performances = new ArrayList<>();

        for (InterviewQuestion question : interview.getQuestions()) {
            if (question.getAnswer() != null && question.getAnswer().isSubmitted()) {
                InterviewEvaluationResponse.QuestionPerformance perf =
                        new InterviewEvaluationResponse.QuestionPerformance();
                perf.setQuestionOrder(question.getQuestionOrder());
                perf.setQuestionText(question.getQuestionText());
                perf.setCategory(question.getCategory());

                InterviewAnswer answer = question.getAnswer();
                perf.setUserAnswer(answer.getAnswerText());
                perf.setFeedback(answer.getFeedback());
                perf.setScore(answer.getScore());
                perf.setRating(answer.getRating());
                perf.setStrengths(answer.getStrengths() != null
                        ? new ArrayList<>(answer.getStrengths()) : List.of());
                perf.setWeaknesses(answer.getImprovements() != null
                        ? new ArrayList<>(answer.getImprovements()) : List.of());

                if (answer.getTimeTaken() != null) {
                    perf.setTimeTakenSeconds(answer.getTimeTaken().getSeconds());
                    perf.setExceedsExpectedTime(
                            question.getExpectedTimeMinutes() != null
                                    && answer.getTimeTaken().getSeconds()
                                    > question.getExpectedTimeMinutes() * 60L
                    );
                }

                performances.add(perf);
            }
        }

        response.setQuestionPerformances(performances);
    }

    private void generateStrengthsAndImprovements(Interview interview, InterviewEvaluationResponse response) {
        List<String> allStrengths = new ArrayList<>();
        List<String> allImprovements = new ArrayList<>();

        for (InterviewAnswer answer : interview.getAnswers()) {
            if (answer.getStrengths() != null) {
                allStrengths.addAll(answer.getStrengths());
            }
            if (answer.getImprovements() != null) {
                allImprovements.addAll(answer.getImprovements());
            }
        }

        List<String> topStrengths = getTopItems(allStrengths, 5);
        List<String> keyImprovements = getTopItems(allImprovements, 5);

        if (topStrengths.isEmpty() && response.getAnsweredQuestions() != null && response.getAnsweredQuestions() > 0) {
            topStrengths = List.of(
                    "Completed interview questions with substantive answers",
                    "Engaged with the " + (interview.getInterviewType() != null
                            ? interview.getInterviewType().getCategoryLabel()
                            : "interview") + " round"
            );
        }
        if (keyImprovements.isEmpty() && response.getAnsweredQuestions() != null && response.getAnsweredQuestions() > 0) {
            keyImprovements = List.of(
                    "Add more concrete examples and measurable outcomes",
                    "Tighten structure: situation → action → result"
            );
        }

        response.setTopStrengths(topStrengths);
        response.setKeyImprovements(keyImprovements);

        if (response.getRecommendedPractice() == null || response.getRecommendedPractice().isEmpty()) {
            response.setRecommendedPractice(buildDefaultPractice(interview, keyImprovements));
        }
    }

    private List<String> buildDefaultPractice(Interview interview, List<String> improvements) {
        String round = interview.getInterviewType() != null
                ? interview.getInterviewType().canonicalize().name()
                : "TECHNICAL";
        return switch (round) {
            case "CODING" -> List.of(
                    "Practice Arrays, HashMaps, and Sliding Window on LeetCode",
                    "Drill Trees, Graphs, and DP with complexity analysis",
                    "Narrate approach out loud in mock coding interviews"
            );
            case "SYSTEM_DESIGN" -> List.of(
                    "Review caching, load balancing, and database sharding patterns",
                    "Practice designing URL shortener, chat, and feed systems",
                    "Document trade-offs for consistency vs availability"
            );
            case "HR", "BEHAVIORAL" -> List.of(
                    "Prepare 5 STAR stories for leadership and conflict",
                    "Practice concise self-introductions tailored to the company",
                    "Book a mock behavioral interview"
            );
            case "MANAGERIAL" -> List.of(
                    "Prepare ownership and stakeholder-conflict scenarios",
                    "Practice prioritization frameworks (RICE / ICE)",
                    "Reflect on mentoring and architecture decision stories"
            );
            case "APTITUDE" -> List.of(
                    "Daily quantitative and logical reasoning drills",
                    "Timed puzzle and data-interpretation sets",
                    "Review percentage, ratio, and series patterns"
            );
            default -> List.of(
                    "Deepen core concepts for " + (interview.getRole() != null ? interview.getRole() : "your role"),
                    improvements.isEmpty() ? "Review weak topics from the scorecard" : "Focus on: " + improvements.get(0),
                    "Schedule another technical mock interview"
            );
        };
    }

    private void generateDetailedFeedback(Interview interview, InterviewEvaluationResponse response) {
        if (response.getDetailedFeedback() != null && !response.getDetailedFeedback().isBlank()) {
            if (response.getRecommendations() == null || response.getRecommendations().isBlank()) {
                List<String> practice = response.getRecommendedPractice() != null
                        ? response.getRecommendedPractice() : List.of();
                response.setRecommendations(practice.isEmpty()
                        ? "Review your question-wise feedback and practice the recommended topics."
                        : String.join("\n", practice.stream().map(p -> "• " + p).toList()));
            }
            return;
        }

        StringBuilder feedback = new StringBuilder();
        double overall = response.getOverallScore() != null ? response.getOverallScore() : 0;
        feedback.append("Overall Performance:\n");
        feedback.append("You scored ").append(String.format("%.0f", overall));
        feedback.append("% (").append(response.getPerformanceLevel() != null
                ? response.getPerformanceLevel() : "UNRATED").append(").\n\n");

        List<String> strengths = response.getTopStrengths() != null ? response.getTopStrengths() : List.of();
        List<String> improvements = response.getKeyImprovements() != null ? response.getKeyImprovements() : List.of();

        if (!strengths.isEmpty()) {
            feedback.append("Key Strengths:\n");
            strengths.forEach(s -> feedback.append("• ").append(s).append("\n"));
        }
        if (!improvements.isEmpty()) {
            feedback.append("\nAreas for Improvement:\n");
            improvements.forEach(i -> feedback.append("• ").append(i).append("\n"));
        }

        response.setDetailedFeedback(feedback.toString());

        List<String> practice = response.getRecommendedPractice() != null
                ? response.getRecommendedPractice() : List.of();
        if (!practice.isEmpty()) {
            response.setRecommendations(String.join("\n", practice.stream().map(p -> "• " + p).toList()));
        } else if (!improvements.isEmpty()) {
            response.setRecommendations(
                    "Based on your performance, we recommend focusing on:\n"
                            + "1. " + improvements.get(0) + "\n"
                            + "2. Improve clarity and structure under time pressure\n"
                            + "3. Review fundamentals in your weaker areas");
        } else {
            response.setRecommendations("Retake a practice interview to build a richer scorecard.");
        }
    }

    private void performSkillsAssessment(Interview interview, InterviewEvaluationResponse response) {
        InterviewEvaluationResponse.SkillsAssessment assessment = new InterviewEvaluationResponse.SkillsAssessment();

        List<String> demonstratedSkills = interview.getAnswers().stream()
                .filter(a -> a.getScore() != null && a.getScore() >= 70.0)
                .map(a -> a.getQuestion().getCategory())
                .filter(Objects::nonNull)
                .distinct()
                .limit(6)
                .collect(Collectors.toList());

        List<String> skillGaps = interview.getAnswers().stream()
                .filter(a -> a.getScore() != null && a.getScore() < 60.0)
                .map(a -> a.getQuestion().getCategory())
                .filter(Objects::nonNull)
                .distinct()
                .limit(4)
                .collect(Collectors.toList());

        if (demonstratedSkills.isEmpty() && response.getTopStrengths() != null) {
            demonstratedSkills = response.getTopStrengths().stream().limit(3).collect(Collectors.toList());
        }
        if (skillGaps.isEmpty() && response.getKeyImprovements() != null) {
            skillGaps = response.getKeyImprovements().stream().limit(3).collect(Collectors.toList());
        }

        assessment.setDemonstratedSkills(demonstratedSkills);
        assessment.setSkillGaps(skillGaps);
        response.setSkillsAssessment(assessment);
    }

    private void generateBenchmarkComparison(Interview interview, InterviewEvaluationResponse response) {
        InterviewEvaluationResponse.BenchmarkComparison comparison = getBenchmarkComparison(
                interview.getUser().getId(),
                interview.getRole()
        );
        response.setBenchmarkComparison(comparison);
    }

    private String extractUserSkills(User user) {
        // First try to get skills from the user's primary/most recent resume
        try {
            Optional<Resume> primaryResume = resumeRepository.findByUserAndIsPrimaryTrue(user);
            if (primaryResume.isPresent() && !primaryResume.get().getSkills().isEmpty()) {
                String skillsFromResume = primaryResume.get().getSkills().stream()
                        .map(Skill::getName)
                        .collect(Collectors.joining(", "));
                if (!skillsFromResume.isBlank()) {
                    return skillsFromResume;
                }
            }
        } catch (Exception e) {
            log.warn("Could not retrieve skills from resume for user {}: {}", user.getId(), e.getMessage());
        }
        
        // Fallback: derive skills from interview question categories
        try {
            List<Interview> recentInterviews = interviewRepository.findByUserAndStatusIn(
                    user, List.of(InterviewStatus.COMPLETED));
            if (!recentInterviews.isEmpty()) {
                String skillsFromInterviews = recentInterviews.stream()
                        .flatMap(i -> i.getQuestions().stream())
                        .map(q -> q.getCategory())
                        .filter(Objects::nonNull)
                        .distinct()
                        .limit(10)
                        .collect(Collectors.joining(", "));
                if (!skillsFromInterviews.isBlank()) {
                    return skillsFromInterviews;
                }
            }
        } catch (Exception e) {
            log.warn("Could not retrieve skills from interviews for user {}: {}", user.getId(), e.getMessage());
        }
        
        // Last resort default
        return "General Software Development, Problem Solving";
    }
    
    @SuppressWarnings("unchecked")
    private void buildSkillGapResponse(SkillGapAnalysisResponse response, Map<String, Object> analysisData) {
        // ---- Skills Matching ----
        SkillGapAnalysisResponse.SkillsMatching skillsMatching = new SkillGapAnalysisResponse.SkillsMatching();
        
        skillsMatching.setMatchedSkills(safeGetStringList(analysisData, "matchedSkills"));
        skillsMatching.setMatchPercentage(safeGetDouble(analysisData, "matchPercentage"));
        skillsMatching.setCategoryMatchPercentages((Map<String, Double>) analysisData.getOrDefault("categoryMatchPercentages", new HashMap<>()));
        
        // Build SkillGap objects
        List<SkillGapAnalysisResponse.SkillGap> skillGaps = new ArrayList<>();
        Object rawGaps = analysisData.get("skillGaps");
        if (rawGaps instanceof List<?> gapList) {
            for (Object gapObj : gapList) {
                if (gapObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> gapMap = (Map<String, Object>) gapObj;
                    SkillGapAnalysisResponse.SkillGap gap = new SkillGapAnalysisResponse.SkillGap();
                    gap.setSkillName((String) gapMap.getOrDefault("skillName", ""));
                    gap.setCurrentLevel((String) gapMap.getOrDefault("currentLevel", "BEGINNER"));
                    gap.setRequiredLevel((String) gapMap.getOrDefault("requiredLevel", "INTERMEDIATE"));
                    gap.setImportance((String) gapMap.getOrDefault("importance", "MEDIUM"));
                    gap.setGapSize((String) gapMap.getOrDefault("gapSize", "MEDIUM"));
                    skillGaps.add(gap);
                }
            }
        }
        skillsMatching.setSkillGaps(skillGaps);
        response.setSkillsMatching(skillsMatching);
        
        // ---- Learning Recommendations ----
        SkillGapAnalysisResponse.LearningRecommendations recommendations = new SkillGapAnalysisResponse.LearningRecommendations();
        Object rawRec = analysisData.get("learningRecommendations");
        if (rawRec instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> recMap = (Map<String, Object>) rawRec;
            recommendations.setImmediateActions(buildLearningResources(recMap.get("immediateActions")));
            recommendations.setShortTermGoals(buildLearningResources(recMap.get("shortTermGoals")));
            recommendations.setLongTermGoals(buildLearningResources(recMap.get("longTermGoals")));
            Object weeksObj = recMap.get("estimatedWeeksToCloseGap");
            if (weeksObj instanceof Number n) {
                recommendations.setEstimatedWeeksToCloseGap(n.intValue());
            }
        } else {
            // Default recommendations when AI response lacks detail
            recommendations.setImmediateActions(List.of());
            recommendations.setShortTermGoals(List.of());
            recommendations.setLongTermGoals(List.of());
            recommendations.setEstimatedWeeksToCloseGap(8);
        }
        response.setLearningRecommendations(recommendations);
        
        // ---- Readiness ----
        Double readiness = safeGetDouble(analysisData, "overallReadiness");
        response.setOverallReadiness(readiness != null ? readiness : 0.0);
        response.setReadinessLevel(determineReadinessLevel(response.getOverallReadiness()));
        
        // ---- Learning Roadmap ----
        List<SkillGapAnalysisResponse.LearningPhase> roadmap = new ArrayList<>();
        Object rawRoadmap = analysisData.get("learningRoadmap");
        if (rawRoadmap instanceof List<?> roadmapList) {
            int phaseNum = 1;
            for (Object phaseObj : roadmapList) {
                if (phaseObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> phaseMap = (Map<String, Object>) phaseObj;
                    SkillGapAnalysisResponse.LearningPhase phase = new SkillGapAnalysisResponse.LearningPhase();
                    phase.setPhaseNumber(phaseNum++);
                    phase.setPhaseName((String) phaseMap.getOrDefault("phaseName", "Phase " + (phaseNum - 1)));
                    phase.setDescription((String) phaseMap.getOrDefault("description", ""));
                    Object dur = phaseMap.get("durationWeeks");
                    phase.setDurationWeeks(dur instanceof Number n ? n.intValue() : 2);
                    phase.setSkillsToAcquire(safeGetStringList(phaseMap, "skillsToAcquire"));
                    phase.setMilestones(safeGetStringList(phaseMap, "milestones"));
                    phase.setResources(buildLearningResources(phaseMap.get("resources")));
                    roadmap.add(phase);
                }
            }
        }
        response.setLearningRoadmap(roadmap);
    }
    
    private List<SkillGapAnalysisResponse.LearningResource> buildLearningResources(Object rawResources) {
        List<SkillGapAnalysisResponse.LearningResource> resources = new ArrayList<>();
        if (rawResources instanceof List<?> resourceList) {
            for (Object resObj : resourceList) {
                if (resObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> resMap = (Map<String, Object>) resObj;
                    SkillGapAnalysisResponse.LearningResource resource = new SkillGapAnalysisResponse.LearningResource();
                    resource.setTitle((String) resMap.getOrDefault("title", ""));
                    resource.setDescription((String) resMap.getOrDefault("description", ""));
                    resource.setResourceType((String) resMap.getOrDefault("resourceType", "PRACTICE"));
                    resource.setProvider((String) resMap.getOrDefault("provider", ""));
                    resource.setUrl((String) resMap.getOrDefault("url", ""));
                    Object hours = resMap.get("estimatedHours");
                    resource.setEstimatedHours(hours instanceof Number n ? n.intValue() : 4);
                    resource.setSkillsCovered(safeGetStringList(resMap, "skillsCovered"));
                    resources.add(resource);
                }
            }
        }
        return resources;
    }
    
    @SuppressWarnings("unchecked")
    private List<String> safeGetStringList(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof List<?> list) {
            return list.stream()
                    .filter(item -> item instanceof String)
                    .map(item -> (String) item)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
    
    private Double safeGetDouble(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val instanceof Number n) return n.doubleValue();
        return null;
    }
    
    private String determineReadinessLevel(Double score) {
        if (score == null) return "SIGNIFICANT_GAP";
        if (score >= 80) return "READY";
        if (score >= 65) return "ALMOST_READY";
        if (score >= 45) return "NEEDS_PREPARATION";
        return "SIGNIFICANT_GAP";
    }
    
    private SkillGapAnalysisResponse.InterviewScoreTrend mapToScoreTrend(Interview interview) {
        SkillGapAnalysisResponse.InterviewScoreTrend trend = new SkillGapAnalysisResponse.InterviewScoreTrend();
        trend.setDate(interview.getCompletedAt().format(DateTimeFormatter.ISO_LOCAL_DATE));
        trend.setScore(interview.getOverallScore());
        trend.setRole(interview.getRole());
        trend.setCompany(interview.getCompany());
        return trend;
    }
    
    private Map<String, Double> calculateCategoryTrends(List<Interview> interviews) {
        // Calculate trends for each category
        Map<String, Double> trends = new HashMap<>();
        trends.put("technical", 0.0);
        trends.put("behavioral", 0.0);
        trends.put("communication", 0.0);
        return trends;
    }
    
    private Map<String, Double> getRoleBenchmarks(String role) {
        // In production, this would query a benchmarks database
        Map<String, Double> benchmarks = new HashMap<>();
        benchmarks.put("average", 75.0);
        benchmarks.put("technical", 80.0);
        benchmarks.put("behavioral", 70.0);
        benchmarks.put("communication", 75.0);
        return benchmarks;
    }
    
    private double calculatePercentileRank(double score, String role) {
        // Simplified percentile calculation
        // In production, this would use actual distribution data
        if (score >= 90) return 95.0;
        if (score >= 80) return 80.0;
        if (score >= 70) return 60.0;
        if (score >= 60) return 40.0;
        return 20.0;
    }
    
    private String generateComparisonMessage(double userScore, double benchmarkScore, double percentile) {
        if (userScore > benchmarkScore) {
            return String.format("You scored above the average for %s role. You're in the top %.0f%% of candidates.",
                    "this", 100 - percentile);
        } else {
            return String.format("You scored below the average for this role. You're in the %.0f percentile of candidates.",
                    percentile);
        }
    }
    
    private Map<String, Double> generateCategoryComparisons(UUID userId, String role) {
        // Generate category-wise comparisons
        Map<String, Double> comparisons = new HashMap<>();
        comparisons.put("technical", 5.0);  // 5% above benchmark
        comparisons.put("behavioral", -3.0); // 3% below benchmark
        return comparisons;
    }
    
    private String determineRating(double score) {
        if (score >= 90) return "EXCELLENT";
        if (score >= 80) return "VERY_GOOD";
        if (score >= 70) return "GOOD";
        if (score >= 60) return "SATISFACTORY";
        if (score >= 50) return "NEEDS_IMPROVEMENT";
        return "POOR";
    }
    
    private String determinePerformanceLevel(double score) {
        if (score >= 85) return "EXCELLENT";
        if (score >= 70) return "GOOD";
        if (score >= 55) return "AVERAGE";
        if (score >= 40) return "BELOW_AVERAGE";
        return "POOR";
    }
    
    private Double calculateCategoryScore(List<InterviewAnswer> answers) {
        if (answers == null || answers.isEmpty()) {
            return null;
        }
        return answers.stream()
                .mapToDouble(InterviewAnswer::getScore)
                .average()
                .orElse(0.0);
    }
    
    private List<String> getTopItems(List<String> items, int limit) {
        Map<String, Long> frequency = items.stream()
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));
        
        return frequency.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
