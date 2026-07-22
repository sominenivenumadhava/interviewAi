package com.interviai.backend.module.analytics.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import com.interviai.backend.common.exception.ResourceNotFoundException;
import java.util.UUID;
import com.interviai.backend.module.analytics.dto.CompanyAnalyticsResponse;
import java.util.UUID;
import com.interviai.backend.module.analytics.dto.UserAnalyticsResponse;
import java.util.UUID;
import com.interviai.backend.module.analytics.service.AnalyticsService;
import java.util.UUID;
import com.interviai.backend.module.interview.entity.Interview;
import java.util.UUID;
import com.interviai.backend.module.interview.entity.InterviewAnswer;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AnalyticsServiceImpl implements AnalyticsService {
    
    private static final Logger log = LoggerFactory.getLogger(AnalyticsServiceImpl.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private InterviewRepository interviewRepository;
    
    @Autowired
    private InterviewAnswerRepository answerRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Override
    @Transactional(readOnly = true)
    public UserAnalyticsResponse getUserAnalytics(UUID userId, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        UserAnalyticsResponse response = new UserAnalyticsResponse();
        response.setUserId(userId);
        
        // Get all user interviews in the date range
        List<Interview> interviews = interviewRepository.findCompletedInterviewsBetween(
                user,
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay()
        );
        
        // Calculate overview metrics
        response.setOverviewMetrics(calculateOverviewMetrics(user, interviews));
        
        // Calculate performance metrics
        response.setPerformanceMetrics(calculatePerformanceMetrics(interviews));
        
        // Calculate skill metrics
        response.setSkillMetrics(calculateSkillMetrics(interviews));
        
        // Calculate time metrics
        response.setTimeMetrics(calculateTimeMetrics(interviews));
        
        // Get recent interview history
        response.setRecentInterviews(getRecentInterviewHistory(interviews, 10));
        
        // Generate charts data
        response.setCharts(generateChartsData(interviews));
        
        return response;
    }
    
    @Override
    public CompanyAnalyticsResponse getCompanyAnalytics(UUID userId, String company) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        List<Interview> companyInterviews = interviewRepository.findByUser(user, null).getContent().stream()
                .filter(i -> company.equalsIgnoreCase(i.getCompany()))
                .collect(Collectors.toList());
        
        CompanyAnalyticsResponse response = new CompanyAnalyticsResponse();
        response.setCompany(company);
        
        // Calculate company overview
        response.setOverview(calculateCompanyOverview(companyInterviews));
        
        // Calculate difficulty distribution
        response.setDifficultyDistribution(calculateDifficultyDistribution(companyInterviews));
        
        // Calculate role statistics
        response.setRoleStatistics(calculateRoleStatistics(companyInterviews));
        
        // Get common questions
        response.setCommonQuestions(getCommonQuestions(companyInterviews));
        
        // Get success stories
        response.setSuccessStories(getSuccessStories(companyInterviews));
        
        // Calculate skill requirements
        response.setSkillRequirements(calculateSkillRequirements(companyInterviews));
        
        return response;
    }
    
    @Override
    public List<UserAnalyticsResponse.CategoryPerformance> getRoleComparison(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        List<Interview> interviews = interviewRepository.findByUserAndStatus(
                user, InterviewStatus.COMPLETED, null).getContent();
        
        Map<String, List<Interview>> interviewsByRole = interviews.stream()
                .collect(Collectors.groupingBy(Interview::getRole));
        
        return interviewsByRole.entrySet().stream()
                .map(entry -> {
                    UserAnalyticsResponse.CategoryPerformance perf = new UserAnalyticsResponse.CategoryPerformance();
                    perf.setCategory(entry.getKey());
                    
                    List<Interview> roleInterviews = entry.getValue();
                    double avgScore = roleInterviews.stream()
                            .filter(i -> i.getOverallScore() != null)
                            .mapToDouble(Interview::getOverallScore)
                            .average()
                            .orElse(0.0);
                    
                    perf.setAverageScore(avgScore);
                    perf.setQuestionCount(roleInterviews.size());
                    perf.setImprovementRate(calculateImprovementRate(roleInterviews));
                    perf.setTrend(determineTrend(roleInterviews));
                    
                    return perf;
                })
                .sorted((a, b) -> Double.compare(b.getAverageScore(), a.getAverageScore()))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<UserAnalyticsResponse.SkillProgress> getSkillProgress(UUID userId, String skillName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        List<Interview> interviews = interviewRepository.findByUserAndStatus(
                user, InterviewStatus.COMPLETED, null).getContent().stream()
                .sorted(Comparator.comparing(Interview::getCompletedAt))
                .collect(Collectors.toList());
        
        Map<String, List<Double>> skillScoresOverTime = new HashMap<>();
        
        // Extract skill scores from interview answers
        for (Interview interview : interviews) {
            for (InterviewAnswer answer : interview.getAnswers()) {
                if (answer.getScore() != null && answer.getQuestion().getCategory() != null) {
                    String skill = answer.getQuestion().getCategory();
                    if (skillName == null || skill.contains(skillName)) {
                        skillScoresOverTime.computeIfAbsent(skill, k -> new ArrayList<>())
                                .add(answer.getScore());
                    }
                }
            }
        }
        
        return skillScoresOverTime.entrySet().stream()
                .map(entry -> {
                    UserAnalyticsResponse.SkillProgress progress = new UserAnalyticsResponse.SkillProgress();
                    progress.setSkillName(entry.getKey());
                    
                    List<Double> scores = entry.getValue();
                    if (!scores.isEmpty()) {
                        progress.setInitialScore(scores.get(0));
                        progress.setCurrentScore(scores.get(scores.size() - 1));
                        progress.setImprovement(progress.getCurrentScore() - progress.getInitialScore());
                        progress.setTrend(progress.getImprovement() > 0 ? "IMPROVING" :
                                        progress.getImprovement() < 0 ? "DECLINING" : "STABLE");
                    }
                    
                    return progress;
                })
                .filter(p -> p.getCurrentScore() != null)
                .sorted((a, b) -> Double.compare(b.getImprovement(), a.getImprovement()))
                .collect(Collectors.toList());
    }
    
    @Override
    public PreparationInsights getPreparationInsights(UUID userId, String targetRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        PreparationInsights insights = new PreparationInsights();
        insights.setTargetRole(targetRole);
        
        // Get recent performance for similar roles
        List<Interview> similarRoleInterviews = interviewRepository.findByUser(user, null).getContent().stream()
                .filter(i -> i.getRole().toLowerCase().contains(targetRole.toLowerCase()) ||
                           targetRole.toLowerCase().contains(i.getRole().toLowerCase()))
                .filter(i -> i.getOverallScore() != null)
                .collect(Collectors.toList());
        
        if (!similarRoleInterviews.isEmpty()) {
            double avgScore = similarRoleInterviews.stream()
                    .mapToDouble(Interview::getOverallScore)
                    .average()
                    .orElse(0.0);
            
            insights.setReadinessScore(avgScore);
            
            // Estimate interviews needed based on current performance
            if (avgScore >= 80) {
                insights.setEstimatedInterviewsNeeded(1);
            } else if (avgScore >= 70) {
                insights.setEstimatedInterviewsNeeded(3);
            } else if (avgScore >= 60) {
                insights.setEstimatedInterviewsNeeded(5);
            } else {
                insights.setEstimatedInterviewsNeeded(10);
            }
        } else {
            insights.setReadinessScore(0.0);
            insights.setEstimatedInterviewsNeeded(10);
        }
        
        // Identify focus areas based on weak performance
        List<String> focusAreas = identifyWeakAreas(user);
        insights.setFocusAreas(focusAreas);
        
        // Identify strong areas
        List<String> strongAreas = identifyStrongAreas(user);
        insights.setStrongAreas(strongAreas);
        
        // Recommend practice hours
        Map<String, Integer> practiceHours = new HashMap<>();
        focusAreas.forEach(area -> practiceHours.put(area, 10));
        insights.setRecommendedPracticeHours(practiceHours);
        
        // Suggest resources
        insights.setSuggestedResources(Arrays.asList(
                "Practice coding problems on LeetCode",
                "Review system design concepts",
                "Prepare behavioral questions using STAR method",
                "Study company-specific technologies"
        ));
        
        return insights;
    }
    
    @Override
    public byte[] exportAnalytics(UUID userId, String format) {
        UserAnalyticsResponse analytics = getUserAnalytics(
                userId,
                LocalDate.now().minusMonths(12),
                LocalDate.now()
        );
        
        try {
            if ("json".equalsIgnoreCase(format)) {
                return objectMapper.writeValueAsBytes(analytics);
            } else if ("csv".equalsIgnoreCase(format)) {
                return generateCSVExport(analytics);
            } else {
                // For PDF, would use a PDF generation library
                return objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(analytics)
                        .getBytes();
            }
        } catch (Exception e) {
            log.error("Error exporting analytics: {}", e.getMessage());
            throw new RuntimeException("Failed to export analytics", e);
        }
    }
    
    // Helper methods
    private UserAnalyticsResponse.OverviewMetrics calculateOverviewMetrics(User user, List<Interview> interviews) {
        UserAnalyticsResponse.OverviewMetrics metrics = new UserAnalyticsResponse.OverviewMetrics();
        
        // Total interviews
        long totalInterviews = interviewRepository.countByUserAndStatus(user, null);
        metrics.setTotalInterviews((int) totalInterviews);
        
        // Completed interviews
        long completedInterviews = interviewRepository.countByUserAndStatus(user, InterviewStatus.COMPLETED);
        metrics.setCompletedInterviews((int) completedInterviews);
        
        // Upcoming interviews
        long upcomingInterviews = interviewRepository.countByUserAndStatus(user, InterviewStatus.SCHEDULED);
        metrics.setUpcomingInterviews((int) upcomingInterviews);
        
        // Average score
        Double avgScore = interviewRepository.getAverageScoreByUser(user);
        metrics.setAverageScore(avgScore != null ? avgScore : 0.0);
        
        // Overall rating
        metrics.setOverallRating(determineRating(metrics.getAverageScore()));
        
        // Improvement rate
        metrics.setImprovementRate(calculateOverallImprovementRate(interviews));
        
        // Streaks
        calculateStreaks(interviews, metrics);
        
        return metrics;
    }
    
    private UserAnalyticsResponse.PerformanceMetrics calculatePerformanceMetrics(List<Interview> interviews) {
        UserAnalyticsResponse.PerformanceMetrics metrics = new UserAnalyticsResponse.PerformanceMetrics();
        
        LocalDate now = LocalDate.now();
        LocalDate lastMonthStart = now.minusMonths(1).withDayOfMonth(1);
        LocalDate currentMonthStart = now.withDayOfMonth(1);
        
        // Current and last month scores
        List<Interview> currentMonthInterviews = filterInterviewsByDateRange(
                interviews, currentMonthStart, now);
        List<Interview> lastMonthInterviews = filterInterviewsByDateRange(
                interviews, lastMonthStart, currentMonthStart.minusDays(1));
        
        metrics.setCurrentMonthScore(calculateAverageScore(currentMonthInterviews));
        metrics.setLastMonthScore(calculateAverageScore(lastMonthInterviews));
        
        // Month over month growth
        if (metrics.getLastMonthScore() > 0) {
            double growth = ((metrics.getCurrentMonthScore() - metrics.getLastMonthScore()) 
                    / metrics.getLastMonthScore()) * 100;
            metrics.setMonthOverMonthGrowth(growth);
        }
        
        // Scores by difficulty
        metrics.setScoresByDifficulty(calculateScoresByDifficulty(interviews));
        
        // Scores by interview type
        metrics.setScoresByInterviewType(calculateScoresByType(interviews));
        
        // Monthly trends
        metrics.setMonthlyTrends(calculateMonthlyTrends(interviews));
        
        // Best performance
        metrics.setBestPerformance(findBestPerformance(interviews));
        
        // Category performance
        metrics.setCategoryPerformance(calculateCategoryPerformance(interviews));
        
        return metrics;
    }
    
    private UserAnalyticsResponse.SkillMetrics calculateSkillMetrics(List<Interview> interviews) {
        UserAnalyticsResponse.SkillMetrics metrics = new UserAnalyticsResponse.SkillMetrics();
        
        Map<String, List<Double>> skillScores = new HashMap<>();
        
        // Collect skill scores from all answers
        for (Interview interview : interviews) {
            for (InterviewAnswer answer : interview.getAnswers()) {
                if (answer.getScore() != null && answer.getQuestion().getCategory() != null) {
                    skillScores.computeIfAbsent(answer.getQuestion().getCategory(), k -> new ArrayList<>())
                            .add(answer.getScore());
                }
            }
        }
        
        // Calculate average scores for each skill
        List<UserAnalyticsResponse.SkillScore> allSkills = skillScores.entrySet().stream()
                .map(entry -> {
                    UserAnalyticsResponse.SkillScore score = new UserAnalyticsResponse.SkillScore();
                    score.setSkillName(entry.getKey());
                    
                    double avg = entry.getValue().stream()
                            .mapToDouble(Double::doubleValue)
                            .average()
                            .orElse(0.0);
                    
                    score.setScore(avg);
                    score.setProficiencyLevel(determineProficiencyLevel(avg));
                    score.setAssessmentCount(entry.getValue().size());
                    
                    return score;
                })
                .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
                .collect(Collectors.toList());
        
        // Top skills
        metrics.setTopSkills(allSkills.stream()
                .limit(5)
                .collect(Collectors.toList()));
        
        // Weak skills
        metrics.setWeakSkills(allSkills.stream()
                .sorted(Comparator.comparingDouble(UserAnalyticsResponse.SkillScore::getScore))
                .limit(5)
                .collect(Collectors.toList()));
        
        // Skill progress
        metrics.setSkillProgress(calculateSkillProgressList(interviews));
        
        metrics.setTotalSkillsAssessed(allSkills.size());
        
        // Skill category scores
        Map<String, Double> categoryScores = new HashMap<>();
        categoryScores.put("Technical", calculateCategoryAverage(allSkills, "technical"));
        categoryScores.put("Behavioral", calculateCategoryAverage(allSkills, "behavioral"));
        categoryScores.put("Problem Solving", calculateCategoryAverage(allSkills, "problem"));
        metrics.setSkillCategoryScores(categoryScores);
        
        return metrics;
    }
    
    private UserAnalyticsResponse.TimeMetrics calculateTimeMetrics(List<Interview> interviews) {
        UserAnalyticsResponse.TimeMetrics metrics = new UserAnalyticsResponse.TimeMetrics();
        
        // Average interview duration
        double avgDuration = interviews.stream()
                .filter(i -> i.getActualDuration() != null)
                .mapToLong(i -> i.getActualDuration().toMinutes())
                .average()
                .orElse(0.0);
        metrics.setAverageInterviewDuration(avgDuration);
        
        // Average time per question
        double avgTimePerQuestion = interviews.stream()
                .flatMap(i -> i.getAnswers().stream())
                .filter(a -> a.getTimeTaken() != null)
                .mapToLong(a -> a.getTimeTaken().getSeconds())
                .average()
                .orElse(0.0) / 60.0; // Convert to minutes
        metrics.setAverageTimePerQuestion(avgTimePerQuestion);
        
        // Total practice hours
        long totalMinutes = interviews.stream()
                .filter(i -> i.getActualDuration() != null)
                .mapToLong(i -> i.getActualDuration().toMinutes())
                .sum();
        metrics.setTotalPracticeHours((int) (totalMinutes / 60));
        
        // Practice hours by month
        Map<String, Integer> hoursByMonth = interviews.stream()
                .filter(i -> i.getCompletedAt() != null && i.getActualDuration() != null)
                .collect(Collectors.groupingBy(
                        i -> i.getCompletedAt().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        Collectors.summingInt(i -> (int) (i.getActualDuration().toMinutes() / 60))
                ));
        metrics.setPracticeHoursByMonth(hoursByMonth);
        
        // Recent sessions
        List<UserAnalyticsResponse.PracticeSession> recentSessions = interviews.stream()
                .filter(i -> i.getCompletedAt() != null)
                .sorted((a, b) -> b.getCompletedAt().compareTo(a.getCompletedAt()))
                .limit(10)
                .map(this::mapToPracticeSession)
                .collect(Collectors.toList());
        metrics.setRecentSessions(recentSessions);
        
        return metrics;
    }
    
    private List<UserAnalyticsResponse.InterviewHistory> getRecentInterviewHistory(
            List<Interview> interviews, int limit) {
        return interviews.stream()
                .filter(i -> i.getCompletedAt() != null)
                .sorted((a, b) -> b.getCompletedAt().compareTo(a.getCompletedAt()))
                .limit(limit)
                .map(this::mapToInterviewHistory)
                .collect(Collectors.toList());
    }
    
    private Map<String, UserAnalyticsResponse.ChartData> generateChartsData(List<Interview> interviews) {
        Map<String, UserAnalyticsResponse.ChartData> charts = new HashMap<>();
        
        // Performance over time chart
        charts.put("performanceOverTime", generatePerformanceOverTimeChart(interviews));
        
        // Score distribution chart
        charts.put("scoreDistribution", generateScoreDistributionChart(interviews));
        
        // Skills radar chart
        charts.put("skillsRadar", generateSkillsRadarChart(interviews));
        
        return charts;
    }
    
    private UserAnalyticsResponse.ChartData generatePerformanceOverTimeChart(List<Interview> interviews) {
        UserAnalyticsResponse.ChartData chartData = new UserAnalyticsResponse.ChartData();
        chartData.setChartType("line");
        
        Map<String, List<Interview>> interviewsByMonth = interviews.stream()
                .filter(i -> i.getCompletedAt() != null)
                .collect(Collectors.groupingBy(
                        i -> i.getCompletedAt().format(DateTimeFormatter.ofPattern("MMM yyyy")),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        
        chartData.setLabels(new ArrayList<>(interviewsByMonth.keySet()));
        
        UserAnalyticsResponse.Dataset dataset = new UserAnalyticsResponse.Dataset();
        dataset.setLabel("Average Score");
        dataset.setData(interviewsByMonth.values().stream()
                .map(this::calculateAverageScore)
                .collect(Collectors.toList()));
        dataset.setBackgroundColor("#4F46E5");
        dataset.setBorderColor("#4F46E5");
        
        chartData.setDatasets(List.of(dataset));
        
        return chartData;
    }
    
    private UserAnalyticsResponse.ChartData generateScoreDistributionChart(List<Interview> interviews) {
        UserAnalyticsResponse.ChartData chartData = new UserAnalyticsResponse.ChartData();
        chartData.setChartType("bar");
        
        List<String> labels = Arrays.asList("0-20", "21-40", "41-60", "61-80", "81-100");
        chartData.setLabels(labels);
        
        int[] distribution = new int[5];
        interviews.stream()
                .filter(i -> i.getOverallScore() != null)
                .forEach(i -> {
                    double score = i.getOverallScore();
                    if (score <= 20) distribution[0]++;
                    else if (score <= 40) distribution[1]++;
                    else if (score <= 60) distribution[2]++;
                    else if (score <= 80) distribution[3]++;
                    else distribution[4]++;
                });
        
        UserAnalyticsResponse.Dataset dataset = new UserAnalyticsResponse.Dataset();
        dataset.setLabel("Number of Interviews");
        dataset.setData(Arrays.stream(distribution)
                .mapToDouble(i -> i)
                .boxed()
                .collect(Collectors.toList()));
        dataset.setBackgroundColor("#10B981");
        
        chartData.setDatasets(List.of(dataset));
        
        return chartData;
    }
    
    private UserAnalyticsResponse.ChartData generateSkillsRadarChart(List<Interview> interviews) {
        UserAnalyticsResponse.ChartData chartData = new UserAnalyticsResponse.ChartData();
        chartData.setChartType("radar");
        
        Map<String, Double> skillScores = calculateAverageSkillScores(interviews);
        
        chartData.setLabels(new ArrayList<>(skillScores.keySet()));
        
        UserAnalyticsResponse.Dataset dataset = new UserAnalyticsResponse.Dataset();
        dataset.setLabel("Skill Level");
        dataset.setData(new ArrayList<>(skillScores.values()));
        dataset.setBackgroundColor("rgba(79, 70, 229, 0.2)");
        dataset.setBorderColor("#4F46E5");
        
        chartData.setDatasets(List.of(dataset));
        
        return chartData;
    }
    
    private CompanyAnalyticsResponse.CompanyOverview calculateCompanyOverview(List<Interview> interviews) {
        CompanyAnalyticsResponse.CompanyOverview overview = new CompanyAnalyticsResponse.CompanyOverview();
        
        overview.setTotalInterviews(interviews.size());
        overview.setAverageScore(calculateAverageScore(interviews));
        
        long successfulInterviews = interviews.stream()
                .filter(i -> i.getOverallScore() != null && i.getOverallScore() >= 70)
                .count();
        overview.setSuccessRate(interviews.isEmpty() ? 0.0 : 
                (successfulInterviews * 100.0) / interviews.size());
        
        // Most common role
        interviews.stream()
                .collect(Collectors.groupingBy(Interview::getRole, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresent(entry -> overview.setMostCommonRole(entry.getKey()));
        
        // Preferred interview type
        interviews.stream()
                .collect(Collectors.groupingBy(i -> i.getInterviewType().name(), Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresent(entry -> overview.setPreferredInterviewType(entry.getKey()));
        
        // Average duration
        double avgDuration = interviews.stream()
                .filter(i -> i.getActualDuration() != null)
                .mapToLong(i -> i.getActualDuration().toMinutes())
                .average()
                .orElse(0.0);
        overview.setAverageDuration((int) avgDuration);
        
        return overview;
    }
    
    private CompanyAnalyticsResponse.DifficultyDistribution calculateDifficultyDistribution(
            List<Interview> interviews) {
        CompanyAnalyticsResponse.DifficultyDistribution distribution = 
                new CompanyAnalyticsResponse.DifficultyDistribution();
        
        Map<String, Long> counts = interviews.stream()
                .collect(Collectors.groupingBy(
                        i -> i.getDifficultyLevel().name(),
                        Collectors.counting()
                ));
        
        distribution.setEasyCount(counts.getOrDefault("EASY", 0L).intValue());
        distribution.setMediumCount(counts.getOrDefault("MEDIUM", 0L).intValue());
        distribution.setHardCount(counts.getOrDefault("HARD", 0L).intValue());
        distribution.setExpertCount(counts.getOrDefault("EXPERT", 0L).intValue());
        
        // Calculate average difficulty
        double avgDifficulty = interviews.stream()
                .mapToInt(i -> i.getDifficultyLevel().getLevel())
                .average()
                .orElse(0.0);
        distribution.setAverageDifficulty(avgDifficulty);
        
        return distribution;
    }
    
    private List<CompanyAnalyticsResponse.RoleStatistics> calculateRoleStatistics(
            List<Interview> interviews) {
        Map<String, List<Interview>> interviewsByRole = interviews.stream()
                .collect(Collectors.groupingBy(Interview::getRole));
        
        return interviewsByRole.entrySet().stream()
                .map(entry -> {
                    CompanyAnalyticsResponse.RoleStatistics stats = 
                            new CompanyAnalyticsResponse.RoleStatistics();
                    stats.setRole(entry.getKey());
                    
                    List<Interview> roleInterviews = entry.getValue();
                    stats.setInterviewCount(roleInterviews.size());
                    stats.setAverageScore(calculateAverageScore(roleInterviews));
                    
                    long successful = roleInterviews.stream()
                            .filter(i -> i.getOverallScore() != null && i.getOverallScore() >= 70)
                            .count();
                    stats.setSuccessRate(roleInterviews.isEmpty() ? 0.0 : 
                            (successful * 100.0) / roleInterviews.size());
                    
                    // Top skills would be extracted from actual interview data
                    stats.setTopSkillsRequired(Arrays.asList(
                            "Problem Solving",
                            "Technical Knowledge",
                            "Communication"
                    ));
                    
                    return stats;
                })
                .sorted((a, b) -> Integer.compare(b.getInterviewCount(), a.getInterviewCount()))
                .collect(Collectors.toList());
    }
    
    private List<CompanyAnalyticsResponse.CommonQuestion> getCommonQuestions(
            List<Interview> interviews) {
        Map<String, Integer> questionFrequency = new HashMap<>();
        Map<String, List<Double>> questionScores = new HashMap<>();
        Map<String, String> questionCategories = new HashMap<>();
        Map<String, String> questionDifficulties = new HashMap<>();
        
        for (Interview interview : interviews) {
            interview.getQuestions().forEach(q -> {
                String question = q.getQuestionText();
                questionFrequency.merge(question, 1, Integer::sum);
                
                if (q.getAnswer() != null && q.getAnswer().getScore() != null) {
                    questionScores.computeIfAbsent(question, k -> new ArrayList<>())
                            .add(q.getAnswer().getScore());
                }
                
                questionCategories.putIfAbsent(question, q.getCategory());
                questionDifficulties.putIfAbsent(question, 
                        q.getDifficultyLevel() != null ? q.getDifficultyLevel().name() : "MEDIUM");
            });
        }
        
        return questionFrequency.entrySet().stream()
                .filter(e -> e.getValue() > 1) // Only questions asked more than once
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    CompanyAnalyticsResponse.CommonQuestion commonQuestion = 
                            new CompanyAnalyticsResponse.CommonQuestion();
                    commonQuestion.setQuestion(entry.getKey());
                    commonQuestion.setFrequency(entry.getValue());
                    commonQuestion.setCategory(questionCategories.get(entry.getKey()));
                    commonQuestion.setDifficulty(questionDifficulties.get(entry.getKey()));
                    
                    List<Double> scores = questionScores.get(entry.getKey());
                    if (scores != null && !scores.isEmpty()) {
                        double avgScore = scores.stream()
                                .mapToDouble(Double::doubleValue)
                                .average()
                                .orElse(0.0);
                        commonQuestion.setAverageScore(avgScore);
                    }
                    
                    return commonQuestion;
                })
                .collect(Collectors.toList());
    }
    
    private List<CompanyAnalyticsResponse.SuccessStory> getSuccessStories(
            List<Interview> interviews) {
        return interviews.stream()
                .filter(i -> i.getOverallScore() != null && i.getOverallScore() >= 85)
                .sorted((a, b) -> Double.compare(b.getOverallScore(), a.getOverallScore()))
                .limit(5)
                .map(interview -> {
                    CompanyAnalyticsResponse.SuccessStory story = 
                            new CompanyAnalyticsResponse.SuccessStory();
                    story.setRole(interview.getRole());
                    story.setScore(interview.getOverallScore());
                    story.setDate(interview.getCompletedAt().toLocalDate());
                    
                    // Extract key strengths from interview
                    List<String> strengths = new ArrayList<>();
                    if (interview.getStrengths() != null) {
                        strengths = Arrays.asList(interview.getStrengths().split(","));
                    }
                    story.setKeyStrengths(strengths);
                    
                    return story;
                })
                .collect(Collectors.toList());
    }
    
    private Map<String, Double> calculateSkillRequirements(List<Interview> interviews) {
        Map<String, Integer> skillFrequency = new HashMap<>();
        
        for (Interview interview : interviews) {
            interview.getQuestions().forEach(q -> {
                if (q.getCategory() != null) {
                    skillFrequency.merge(q.getCategory(), 1, Integer::sum);
                }
            });
        }
        
        int total = skillFrequency.values().stream().mapToInt(Integer::intValue).sum();
        
        return skillFrequency.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> (e.getValue() * 100.0) / total
                ));
    }
    
    // Utility methods
    private double calculateAverageScore(List<Interview> interviews) {
        return interviews.stream()
                .filter(i -> i.getOverallScore() != null)
                .mapToDouble(Interview::getOverallScore)
                .average()
                .orElse(0.0);
    }
    
    private List<Interview> filterInterviewsByDateRange(
            List<Interview> interviews, LocalDate start, LocalDate end) {
        return interviews.stream()
                .filter(i -> i.getCompletedAt() != null)
                .filter(i -> {
                    LocalDate completedDate = i.getCompletedAt().toLocalDate();
                    return !completedDate.isBefore(start) && !completedDate.isAfter(end);
                })
                .collect(Collectors.toList());
    }
    
    private Map<String, Double> calculateScoresByDifficulty(List<Interview> interviews) {
        return interviews.stream()
                .filter(i -> i.getOverallScore() != null)
                .collect(Collectors.groupingBy(
                        i -> i.getDifficultyLevel().name(),
                        Collectors.averagingDouble(Interview::getOverallScore)
                ));
    }
    
    private Map<String, Double> calculateScoresByType(List<Interview> interviews) {
        return interviews.stream()
                .filter(i -> i.getOverallScore() != null)
                .collect(Collectors.groupingBy(
                        i -> i.getInterviewType().name(),
                        Collectors.averagingDouble(Interview::getOverallScore)
                ));
    }
    
    private List<UserAnalyticsResponse.PerformanceTrend> calculateMonthlyTrends(
            List<Interview> interviews) {
        Map<Month, List<Interview>> interviewsByMonth = interviews.stream()
                .filter(i -> i.getCompletedAt() != null)
                .collect(Collectors.groupingBy(
                        i -> i.getCompletedAt().getMonth(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
        
        List<UserAnalyticsResponse.PerformanceTrend> trends = new ArrayList<>();
        Month previousMonth = null;
        Double previousScore = null;
        
        for (Map.Entry<Month, List<Interview>> entry : interviewsByMonth.entrySet()) {
            UserAnalyticsResponse.PerformanceTrend trend = new UserAnalyticsResponse.PerformanceTrend();
            trend.setMonth(entry.getKey().name());
            trend.setAverageScore(calculateAverageScore(entry.getValue()));
            trend.setInterviewCount(entry.getValue().size());
            
            if (previousScore != null) {
                double growthRate = ((trend.getAverageScore() - previousScore) / previousScore) * 100;
                trend.setGrowthRate(growthRate);
            } else {
                trend.setGrowthRate(0.0);
            }
            
            trends.add(trend);
            previousMonth = entry.getKey();
            previousScore = trend.getAverageScore();
        }
        
        return trends;
    }
    
    private UserAnalyticsResponse.TopPerformance findBestPerformance(List<Interview> interviews) {
        return interviews.stream()
                .filter(i -> i.getOverallScore() != null)
                .max(Comparator.comparing(Interview::getOverallScore))
                .map(interview -> {
                    UserAnalyticsResponse.TopPerformance performance = 
                            new UserAnalyticsResponse.TopPerformance();
                    performance.setRole(interview.getRole());
                    performance.setCompany(interview.getCompany());
                    performance.setScore(interview.getOverallScore());
                    performance.setDate(interview.getCompletedAt().toLocalDate());
                    return performance;
                })
                .orElse(null);
    }
    
    private Map<String, UserAnalyticsResponse.CategoryPerformance> calculateCategoryPerformance(
            List<Interview> interviews) {
        Map<String, List<Double>> categoryScores = new HashMap<>();
        Map<String, Integer> categoryQuestionCounts = new HashMap<>();
        
        for (Interview interview : interviews) {
            for (InterviewAnswer answer : interview.getAnswers()) {
                if (answer.getScore() != null && answer.getQuestion().getCategory() != null) {
                    String category = answer.getQuestion().getCategory();
                    categoryScores.computeIfAbsent(category, k -> new ArrayList<>())
                            .add(answer.getScore());
                    categoryQuestionCounts.merge(category, 1, Integer::sum);
                }
            }
        }
        
        return categoryScores.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            UserAnalyticsResponse.CategoryPerformance perf = 
                                    new UserAnalyticsResponse.CategoryPerformance();
                            perf.setCategory(entry.getKey());
                            
                            List<Double> scores = entry.getValue();
                            perf.setAverageScore(scores.stream()
                                    .mapToDouble(Double::doubleValue)
                                    .average()
                                    .orElse(0.0));
                            
                            perf.setQuestionCount(categoryQuestionCounts.get(entry.getKey()));
                            perf.setImprovementRate(calculateImprovementRateFromScores(scores));
                            perf.setTrend(determineTrendFromScores(scores));
                            
                            return perf;
                        }
                ));
    }
    
    private void calculateStreaks(List<Interview> interviews, 
                                   UserAnalyticsResponse.OverviewMetrics metrics) {
        if (interviews.isEmpty()) {
            metrics.setCurrentStreak(0);
            metrics.setLongestStreak(0);
            return;
        }
        
        // Sort by date
        List<Interview> sortedInterviews = interviews.stream()
                .filter(i -> i.getCompletedAt() != null)
                .sorted(Comparator.comparing(Interview::getCompletedAt))
                .collect(Collectors.toList());
        
        int currentStreak = 0;
        int longestStreak = 0;
        LocalDate lastDate = null;
        
        for (Interview interview : sortedInterviews) {
            LocalDate interviewDate = interview.getCompletedAt().toLocalDate();
            
            if (lastDate == null || ChronoUnit.DAYS.between(lastDate, interviewDate) == 1) {
                currentStreak++;
            } else if (ChronoUnit.DAYS.between(lastDate, interviewDate) > 1) {
                longestStreak = Math.max(longestStreak, currentStreak);
                currentStreak = 1;
            }
            
            lastDate = interviewDate;
        }
        
        longestStreak = Math.max(longestStreak, currentStreak);
        
        // Check if streak is still active
        if (lastDate != null && ChronoUnit.DAYS.between(lastDate, LocalDate.now()) > 1) {
            currentStreak = 0;
        }
        
        metrics.setCurrentStreak(currentStreak);
        metrics.setLongestStreak(longestStreak);
    }
    
    private double calculateOverallImprovementRate(List<Interview> interviews) {
        if (interviews.size() < 2) {
            return 0.0;
        }
        
        List<Interview> sortedInterviews = interviews.stream()
                .filter(i -> i.getOverallScore() != null && i.getCompletedAt() != null)
                .sorted(Comparator.comparing(Interview::getCompletedAt))
                .collect(Collectors.toList());
        
        if (sortedInterviews.size() < 2) {
            return 0.0;
        }
        
        double firstScore = sortedInterviews.get(0).getOverallScore();
        double lastScore = sortedInterviews.get(sortedInterviews.size() - 1).getOverallScore();
        
        return ((lastScore - firstScore) / firstScore) * 100;
    }
    
    private double calculateImprovementRate(List<Interview> interviews) {
        List<Double> scores = interviews.stream()
                .filter(i -> i.getOverallScore() != null)
                .sorted(Comparator.comparing(Interview::getCompletedAt))
                .map(Interview::getOverallScore)
                .collect(Collectors.toList());
        
        return calculateImprovementRateFromScores(scores);
    }
    
    private double calculateImprovementRateFromScores(List<Double> scores) {
        if (scores.size() < 2) {
            return 0.0;
        }
        
        double firstScore = scores.get(0);
        double lastScore = scores.get(scores.size() - 1);
        
        return ((lastScore - firstScore) / firstScore) * 100;
    }
    
    private String determineTrend(List<Interview> interviews) {
        List<Double> scores = interviews.stream()
                .filter(i -> i.getOverallScore() != null)
                .sorted(Comparator.comparing(Interview::getCompletedAt))
                .map(Interview::getOverallScore)
                .collect(Collectors.toList());
        
        return determineTrendFromScores(scores);
    }
    
    private String determineTrendFromScores(List<Double> scores) {
        if (scores.size() < 2) {
            return "STABLE";
        }
        
        // Simple trend analysis - compare last few scores with earlier ones
        int halfSize = scores.size() / 2;
        double firstHalfAvg = scores.subList(0, halfSize).stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        double secondHalfAvg = scores.subList(halfSize, scores.size()).stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        
        double difference = secondHalfAvg - firstHalfAvg;
        
        if (difference > 5) {
            return "IMPROVING";
        } else if (difference < -5) {
            return "DECLINING";
        } else {
            return "STABLE";
        }
    }
    
    private String determineRating(double score) {
        if (score >= 90) return "EXCELLENT";
        if (score >= 80) return "VERY_GOOD";
        if (score >= 70) return "GOOD";
        if (score >= 60) return "SATISFACTORY";
        if (score >= 50) return "NEEDS_IMPROVEMENT";
        return "POOR";
    }
    
    private String determineProficiencyLevel(double score) {
        if (score >= 90) return "EXPERT";
        if (score >= 80) return "PROFICIENT";
        if (score >= 70) return "COMPETENT";
        if (score >= 60) return "DEVELOPING";
        return "NOVICE";
    }
    
    private List<UserAnalyticsResponse.SkillProgress> calculateSkillProgressList(
            List<Interview> interviews) {
        Map<String, List<Double>> skillScoresOverTime = new HashMap<>();
        
        interviews.stream()
                .sorted(Comparator.comparing(Interview::getCompletedAt))
                .forEach(interview -> {
                    interview.getAnswers().stream()
                            .filter(a -> a.getScore() != null && a.getQuestion().getCategory() != null)
                            .forEach(answer -> {
                                String skill = answer.getQuestion().getCategory();
                                skillScoresOverTime.computeIfAbsent(skill, k -> new ArrayList<>())
                                        .add(answer.getScore());
                            });
                });
        
        return skillScoresOverTime.entrySet().stream()
                .filter(e -> e.getValue().size() >= 2)
                .map(entry -> {
                    UserAnalyticsResponse.SkillProgress progress = 
                            new UserAnalyticsResponse.SkillProgress();
                    progress.setSkillName(entry.getKey());
                    
                    List<Double> scores = entry.getValue();
                    progress.setInitialScore(scores.get(0));
                    progress.setCurrentScore(scores.get(scores.size() - 1));
                    progress.setImprovement(progress.getCurrentScore() - progress.getInitialScore());
                    progress.setTrend(determineTrendFromScores(scores));
                    
                    return progress;
                })
                .sorted((a, b) -> Double.compare(b.getImprovement(), a.getImprovement()))
                .limit(10)
                .collect(Collectors.toList());
    }
    
    private UserAnalyticsResponse.InterviewHistory mapToInterviewHistory(Interview interview) {
        UserAnalyticsResponse.InterviewHistory history = new UserAnalyticsResponse.InterviewHistory();
        history.setSessionId(interview.getSessionId());
        history.setRole(interview.getRole());
        history.setCompany(interview.getCompany());
        history.setDate(interview.getCompletedAt().toLocalDate());
        history.setScore(interview.getOverallScore());
        history.setRating(determineRating(interview.getOverallScore() != null ? 
                interview.getOverallScore() : 0));
        history.setDifficulty(interview.getDifficultyLevel().name());
        history.setType(interview.getInterviewType().name());
        
        if (interview.getActualDuration() != null) {
            history.setDuration((int) interview.getActualDuration().toMinutes());
        }
        
        return history;
    }
    
    private UserAnalyticsResponse.PracticeSession mapToPracticeSession(Interview interview) {
        UserAnalyticsResponse.PracticeSession session = new UserAnalyticsResponse.PracticeSession();
        session.setDate(interview.getCompletedAt().toLocalDate());
        
        if (interview.getActualDuration() != null) {
            session.setDurationMinutes((int) interview.getActualDuration().toMinutes());
        }
        
        session.setType(interview.getInterviewType().name());
        session.setScore(interview.getOverallScore());
        
        return session;
    }
    
    private Map<String, Double> calculateAverageSkillScores(List<Interview> interviews) {
        Map<String, List<Double>> skillScores = new HashMap<>();
        
        for (Interview interview : interviews) {
            for (InterviewAnswer answer : interview.getAnswers()) {
                if (answer.getScore() != null && answer.getQuestion().getCategory() != null) {
                    skillScores.computeIfAbsent(answer.getQuestion().getCategory(), 
                            k -> new ArrayList<>())
                            .add(answer.getScore());
                }
            }
        }
        
        return skillScores.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream()
                                .mapToDouble(Double::doubleValue)
                                .average()
                                .orElse(0.0)
                ));
    }
    
    private double calculateCategoryAverage(List<UserAnalyticsResponse.SkillScore> skills, 
                                            String categoryKeyword) {
        return skills.stream()
                .filter(s -> s.getSkillName().toLowerCase().contains(categoryKeyword))
                .mapToDouble(UserAnalyticsResponse.SkillScore::getScore)
                .average()
                .orElse(0.0);
    }
    
    private List<String> identifyWeakAreas(User user) {
        // This would analyze user's performance to identify weak areas
        return Arrays.asList("System Design", "Algorithm Complexity", "Behavioral Questions");
    }
    
    private List<String> identifyStrongAreas(User user) {
        // This would analyze user's performance to identify strong areas
        return Arrays.asList("Data Structures", "Problem Solving", "Communication");
    }
    
    private byte[] generateCSVExport(UserAnalyticsResponse analytics) {
        StringBuilder csv = new StringBuilder();
        
        // Header
        csv.append("Interview Analytics Report\n");
        csv.append("User ID,").append(analytics.getUserId()).append("\n\n");
        
        // Overview
        csv.append("Overview Metrics\n");
        csv.append("Total Interviews,").append(analytics.getOverviewMetrics().getTotalInterviews()).append("\n");
        csv.append("Average Score,").append(analytics.getOverviewMetrics().getAverageScore()).append("\n");
        csv.append("Overall Rating,").append(analytics.getOverviewMetrics().getOverallRating()).append("\n\n");
        
        // Interview History
        csv.append("Recent Interviews\n");
        csv.append("Date,Role,Company,Score,Rating,Duration\n");
        
        for (UserAnalyticsResponse.InterviewHistory history : analytics.getRecentInterviews()) {
            csv.append(history.getDate()).append(",")
               .append(history.getRole()).append(",")
               .append(history.getCompany()).append(",")
               .append(history.getScore()).append(",")
               .append(history.getRating()).append(",")
               .append(history.getDuration()).append("\n");
        }
        
        return csv.toString().getBytes();
    }
}
