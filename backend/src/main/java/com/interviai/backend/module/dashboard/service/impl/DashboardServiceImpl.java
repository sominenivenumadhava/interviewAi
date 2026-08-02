package com.interviai.backend.module.dashboard.service.impl;

import com.interviai.backend.common.exception.ResourceNotFoundException;
import com.interviai.backend.module.analytics.service.AnalyticsService;
import com.interviai.backend.module.dashboard.dto.DashboardResponse;
import com.interviai.backend.module.dashboard.service.DashboardService;
import com.interviai.backend.module.evaluation.service.EvaluationService;
import com.interviai.backend.module.interview.entity.Interview;
import com.interviai.backend.module.interview.entity.InterviewAnswer;
import com.interviai.backend.module.interview.entity.InterviewQuestion;
import com.interviai.backend.module.interview.enums.InterviewStatus;
import com.interviai.backend.module.interview.repository.InterviewRepository;
import com.interviai.backend.module.user.entity.User;
import com.interviai.backend.module.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class DashboardServiceImpl implements DashboardService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private InterviewRepository interviewRepository;
    
    @Autowired
    private AnalyticsService analyticsService;
    
    @Autowired
    private EvaluationService evaluationService;
    
    @Override
    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.CacheEvict(value = "dashboards", key = "#userId", beforeInvocation = true)
    public DashboardResponse getDashboard(java.util.UUID userId) {
        return buildDashboard(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.CacheEvict(value = "dashboards", key = "#userId", beforeInvocation = true)
    public DashboardResponse refreshDashboard(java.util.UUID userId) {
        return buildDashboard(userId);
    }
    
    private DashboardResponse buildDashboard(java.util.UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        DashboardResponse dashboard = new DashboardResponse();
        
        // Build user summary
        dashboard.setUserSummary(buildUserSummary(user));
        
        // Build interview stats
        dashboard.setInterviewStats(buildInterviewStats(user));
        
        // Build performance overview
        dashboard.setPerformanceOverview(buildPerformanceOverview(user));
        
        // Build recent activities
        dashboard.setRecentActivities(buildRecentActivities(user));
        
        // Build upcoming interviews
        dashboard.setUpcomingInterviews(buildUpcomingInterviews(user));
        
        // Build skill snapshot
        dashboard.setSkillSnapshot(buildSkillSnapshot(user));
        
        // Build action items
        dashboard.setActionItems(buildActionItems(user));
        
        // Build quick insights
        dashboard.setQuickInsights(buildQuickInsights(user));
        
        return dashboard;
    }
    
    private DashboardResponse.UserSummary buildUserSummary(User user) {
        DashboardResponse.UserSummary summary = new DashboardResponse.UserSummary();
        summary.setUserId(user.getId());
        summary.setFullName(user.getFirstName() + " " + user.getLastName());
        summary.setEmail(user.getEmail());
        summary.setCurrentLevel(determineUserLevel(user));
        summary.setTotalPracticeHours(calculateTotalPracticeHours(user));
        summary.setMemberSince(user.getCreatedAt()); // already LocalDateTime
        summary.setHasActiveSubscription(user.getIsActive());
        summary.setSubscriptionPlan("Free"); // Would come from subscription service
        return summary;
    }
    
    private DashboardResponse.InterviewStats buildInterviewStats(User user) {
        DashboardResponse.InterviewStats stats = new DashboardResponse.InterviewStats();
        
        // Total interviews
        long totalInterviews = interviewRepository.countByUser(user);
        stats.setTotalInterviews((int) totalInterviews);
        
        // Completed this month
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        List<Interview> allInterviews = interviewRepository.findByUser(user);
        List<Interview> monthInterviews = allInterviews.stream()
                .filter(i -> {
                    LocalDateTime dt = i.getCompletedAt() != null ? i.getCompletedAt() : i.getCreatedAt();
                    return dt != null && !dt.isBefore(monthStart);
                })
                .collect(Collectors.toList());
        stats.setCompletedThisMonth(monthInterviews.size());
        
        // Upcoming this week
        List<Interview> upcomingInterviews = interviewRepository.findUpcomingInterviews(
                user, InterviewStatus.SCHEDULED);
        long upcomingThisWeek = upcomingInterviews.stream()
                .filter(i -> i.getScheduledAt() != null && 
                       i.getScheduledAt().isBefore(LocalDateTime.now().plusWeeks(1)))
                .count();
        stats.setUpcomingThisWeek((int) upcomingThisWeek);
        
        // Success rate
        long successfulInterviews = monthInterviews.stream()
                .filter(i -> i.getOverallScore() != null && i.getOverallScore() >= 70)
                .count();
        stats.setSuccessRate(monthInterviews.isEmpty() ? 0.0 : 
                (successfulInterviews * 100.0) / monthInterviews.size());
        
        // Current streak
        stats.setCurrentStreak(calculateCurrentStreak(user));
        
        // Most practiced role
        List<String> roles = interviewRepository.findDistinctRolesByUser(user);
        if (!roles.isEmpty()) {
            stats.setMostPracticedRole(roles.get(0));
        }
        
        // Preferred difficulty
        stats.setPreferredDifficulty("MEDIUM");
        
        return stats;
    }
    
    private DashboardResponse.PerformanceOverview buildPerformanceOverview(User user) {
        DashboardResponse.PerformanceOverview overview = new DashboardResponse.PerformanceOverview();
        
        List<Interview> allInterviews = interviewRepository.findByUser(user);
        
        if (!allInterviews.isEmpty()) {
            List<Interview> sorted = allInterviews.stream()
                    .sorted((a, b) -> {
                        LocalDateTime dtA = a.getCompletedAt() != null ? a.getCompletedAt() : a.getCreatedAt();
                        LocalDateTime dtB = b.getCompletedAt() != null ? b.getCompletedAt() : b.getCreatedAt();
                        if (dtA == null) return 1;
                        if (dtB == null) return -1;
                        return dtA.compareTo(dtB);
                    })
                    .collect(Collectors.toList());
            
            Interview lastInterview = sorted.get(sorted.size() - 1);
            Double lastScore = lastInterview.getOverallScore();
            if (lastScore != null) {
                overview.setCurrentScore(lastScore);
                if (sorted.size() > 1) {
                    Interview prev = sorted.get(sorted.size() - 2);
                    if (prev.getOverallScore() != null) {
                        overview.setPreviousScore(prev.getOverallScore());
                        overview.setImprovement(lastScore - prev.getOverallScore());
                        overview.setTrend(overview.getImprovement() > 0 ? "UP" : 
                                         overview.getImprovement() < 0 ? "DOWN" : "STABLE");
                    }
                }
            } else {
                overview.setCurrentScore(0.0);
            }
            
            // Average score
            double avgScore = sorted.stream()
                    .mapToDouble(i -> i.getOverallScore() != null ? i.getOverallScore() : 0.0)
                    .average()
                    .orElse(0.0);
            overview.setAverageScore(avgScore);
            
            // Best score
            sorted.stream()
                    .max(Comparator.comparing(i -> i.getOverallScore() != null ? i.getOverallScore() : 0.0))
                    .ifPresent(best -> {
                        overview.setBestScore(best.getOverallScore() != null ? best.getOverallScore() : 0.0);
                        overview.setBestScoreRole(best.getRole());
                    });
            
            // Last 30 days trend
            overview.setLast30DaysTrend(buildScoreTrend(sorted));
        } else {
            overview.setCurrentScore(0.0);
            overview.setTrend("NO_DATA");
            overview.setLast30DaysTrend(new ArrayList<>());
        }
        
        return overview;
    }
    
    private List<DashboardResponse.RecentActivity> buildRecentActivities(User user) {
        List<DashboardResponse.RecentActivity> activities = new ArrayList<>();
        
        List<Interview> allInterviews = interviewRepository.findByUser(user);
        
        for (Interview interview : allInterviews) {
            DashboardResponse.RecentActivity activity = new DashboardResponse.RecentActivity();
            activity.setType("INTERVIEW_COMPLETED");
            activity.setTitle("Completed " + interview.getRole() + " interview");
            double score = interview.getOverallScore() != null ? interview.getOverallScore() : 0.0;
            activity.setDescription(String.format("Score: %.1f%% | %s difficulty", 
                    score, interview.getDifficultyLevel()));
            activity.setTimestamp(interview.getCompletedAt() != null ? interview.getCompletedAt() : interview.getCreatedAt());
            activity.setIcon("check-circle");
            activity.setLink("/interviews/" + interview.getSessionId());
            activities.add(activity);
        }
        
        if (!activities.isEmpty()) {
            DashboardResponse.RecentActivity skillActivity = new DashboardResponse.RecentActivity();
            skillActivity.setType("SKILL_IMPROVED");
            skillActivity.setTitle("Improved in Problem Solving");
            skillActivity.setDescription("+15% improvement in recent interviews");
            skillActivity.setTimestamp(LocalDateTime.now().minusDays(1));
            skillActivity.setIcon("trending-up");
            skillActivity.setLink("/analytics/skills");
            activities.add(skillActivity);
        }
        
        activities.sort((a, b) -> {
            if (a.getTimestamp() == null) return 1;
            if (b.getTimestamp() == null) return -1;
            return b.getTimestamp().compareTo(a.getTimestamp());
        });
        
        return activities.stream().limit(10).collect(Collectors.toList());
    }
    
    private List<DashboardResponse.UpcomingInterview> buildUpcomingInterviews(User user) {
        List<Interview> upcomingInterviews = interviewRepository.findUpcomingInterviews(
                user, InterviewStatus.SCHEDULED);
        
        return upcomingInterviews.stream()
                .limit(5)
                .map(interview -> {
                    DashboardResponse.UpcomingInterview upcoming = new DashboardResponse.UpcomingInterview();
                    upcoming.setId(interview.getId());
                    upcoming.setSessionId(interview.getSessionId());
                    upcoming.setRole(interview.getRole());
                    upcoming.setCompany(interview.getCompany());
                    upcoming.setScheduledAt(interview.getScheduledAt());
                    upcoming.setDifficulty(interview.getDifficultyLevel().name());
                    upcoming.setType(interview.getInterviewType().name());
                    upcoming.setDurationMinutes(interview.getDurationMinutes());
                    upcoming.setStatus(interview.getStatus().name());
                    return upcoming;
                })
                .collect(Collectors.toList());
    }
    
    private DashboardResponse.SkillSnapshot buildSkillSnapshot(User user) {
        DashboardResponse.SkillSnapshot snapshot = new DashboardResponse.SkillSnapshot();
        
        // Gather all completed interviews and their answers
        List<Interview> completedInterviews = interviewRepository.findByUserAndStatus(
                user, InterviewStatus.COMPLETED, Pageable.unpaged()).getContent();
        
        // Group answer scores by question category
        Map<String, List<Double>> scoresByCategory = new HashMap<>();
        for (Interview interview : completedInterviews) {
            for (InterviewAnswer answer : interview.getAnswers()) {
                if (answer.getScore() != null && answer.getQuestion() != null
                        && answer.getQuestion().getCategory() != null) {
                    scoresByCategory
                            .computeIfAbsent(answer.getQuestion().getCategory(), k -> new ArrayList<>())
                            .add(answer.getScore());
                }
            }
        }
        
        // Calculate average per category
        List<Map.Entry<String, Double>> categoryAverages = scoresByCategory.entrySet().stream()
                .map(e -> Map.entry(e.getKey(),
                        e.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0)))
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toList());
        
        // Top skills (highest scoring categories)
        List<DashboardResponse.TopSkill> topSkills = new ArrayList<>();
        int rank = 1;
        for (Map.Entry<String, Double> entry : categoryAverages.stream().limit(3).toList()) {
            String level = entry.getValue() >= 85 ? "EXPERT" :
                           entry.getValue() >= 70 ? "PROFICIENT" : "COMPETENT";
            topSkills.add(createTopSkill(entry.getKey(), entry.getValue(), level, rank++));
        }
        snapshot.setTopSkills(topSkills);
        
        // Improving skills: categories where the last score > first score
        List<DashboardResponse.ImprovingSkill> improvingSkills = new ArrayList<>();
        for (Map.Entry<String, List<Double>> entry : scoresByCategory.entrySet()) {
            List<Double> scores = entry.getValue();
            if (scores.size() >= 2) {
                double first = scores.get(0);
                double last = scores.get(scores.size() - 1);
                double rate = last - first;
                if (rate > 0) {
                    improvingSkills.add(createImprovingSkill(entry.getKey(), rate, "UP"));
                }
            }
        }
        improvingSkills.sort((a, b) -> Double.compare(b.getImprovementRate(), a.getImprovementRate()));
        snapshot.setImprovingSkills(improvingSkills.stream().limit(3).collect(Collectors.toList()));
        
        // Weak skills (lowest scoring categories with score < 65)
        List<DashboardResponse.WeakSkill> weakSkills = categoryAverages.stream()
                .filter(e -> e.getValue() < 65.0)
                .sorted(Map.Entry.comparingByValue())
                .limit(3)
                .map(e -> createWeakSkill(e.getKey(), e.getValue(),
                        "Practice more " + e.getKey() + " questions"))
                .collect(Collectors.toList());
        snapshot.setWeakSkills(weakSkills);
        
        snapshot.setTotalSkillsAssessed(categoryAverages.size());
        snapshot.setLastAssessedAt(completedInterviews.stream()
                .filter(i -> i.getCompletedAt() != null)
                .map(Interview::getCompletedAt)
                .max(Comparator.naturalOrder())
                .orElse(null));
        
        return snapshot;
    }
    
    private List<DashboardResponse.ActionItem> buildActionItems(User user) {
        List<DashboardResponse.ActionItem> actionItems = new ArrayList<>();
        
        // Get skill gap insights
        AnalyticsService.PreparationInsights insights = 
                analyticsService.getPreparationInsights(user.getId(), "Software Engineer");
        
        // Create action items based on insights
        if (insights.getFocusAreas() != null && !insights.getFocusAreas().isEmpty()) {
            DashboardResponse.ActionItem practiceItem = new DashboardResponse.ActionItem();
            practiceItem.setTitle("Practice " + insights.getFocusAreas().get(0));
            practiceItem.setDescription("You need more practice in this area based on recent performance");
            practiceItem.setPriority("HIGH");
            practiceItem.setCategory("PRACTICE");
            practiceItem.setActionUrl("/practice?topic=" + insights.getFocusAreas().get(0));
            practiceItem.setEstimatedMinutes(30);
            practiceItem.setIcon("code");
            actionItems.add(practiceItem);
        }
        
        // Add review action if recent interview completed
        List<Interview> recentCompleted = interviewRepository.findByUserAndStatus(
                user, InterviewStatus.COMPLETED, PageRequest.of(0, 1)).getContent();
        if (!recentCompleted.isEmpty()) {
            DashboardResponse.ActionItem reviewItem = new DashboardResponse.ActionItem();
            reviewItem.setTitle("Review your last interview");
            reviewItem.setDescription("Analyze your performance and feedback");
            reviewItem.setPriority("MEDIUM");
            reviewItem.setCategory("REVIEW");
            reviewItem.setActionUrl("/interviews/" + recentCompleted.get(0).getSessionId() + "/evaluation");
            reviewItem.setEstimatedMinutes(15);
            reviewItem.setIcon("file-text");
            actionItems.add(reviewItem);
        }
        
        // Add learning recommendation
        DashboardResponse.ActionItem learningItem = new DashboardResponse.ActionItem();
        learningItem.setTitle("Learn about System Design");
        learningItem.setDescription("Strengthen your system design skills");
        learningItem.setPriority("MEDIUM");
        learningItem.setCategory("LEARNING");
        learningItem.setActionUrl("/resources/system-design");
        learningItem.setEstimatedMinutes(45);
        learningItem.setIcon("book");
        actionItems.add(learningItem);
        
        return actionItems;
    }
    
    private DashboardResponse.QuickInsights buildQuickInsights(User user) {
        DashboardResponse.QuickInsights insights = new DashboardResponse.QuickInsights();
        
        // Daily tip
        insights.setDailyTip(getDailyTip());
        
        // Weekly goal
        insights.setWeeklyGoal("Complete 5 interviews");
        insights.setWeeklyGoalProgress(calculateWeeklyProgress(user));
        
        // Motivational quote
        insights.setMotivationalQuote(getMotivationalQuote());
        
        // Recommended topics
        insights.setRecommendedTopics(Arrays.asList(
                "Advanced Data Structures",
                "System Design Patterns",
                "Behavioral Interview Techniques"
        ));
        
        // Next milestone
        insights.setNextMilestone("Interview Master");
        insights.setPointsToNextLevel(calculatePointsToNextLevel(user));
        
        return insights;
    }
    
    // Helper methods
    private String determineUserLevel(User user) {
        Double avgScore = interviewRepository.getAverageScoreByUser(user);
        if (avgScore == null) return "Beginner";
        if (avgScore >= 90) return "Expert";
        if (avgScore >= 80) return "Advanced";
        if (avgScore >= 70) return "Intermediate";
        if (avgScore >= 60) return "Developing";
        return "Beginner";
    }
    
    private Integer calculateTotalPracticeHours(User user) {
        List<Interview> allInterviews = interviewRepository.findByUser(user);
        long totalMinutes = allInterviews.stream()
                .mapToLong(i -> {
                    if (i.getActualDuration() != null) return i.getActualDuration().toMinutes();
                    if (i.getDurationMinutes() != null) return i.getDurationMinutes();
                    return 15L;
                })
                .sum();
        return (int) Math.ceil((double) totalMinutes / 60.0);
    }
    
    private Integer calculateCurrentStreak(User user) {
        List<Interview> allInterviews = interviewRepository.findByUser(user);
        if (allInterviews.isEmpty()) return 0;
        return 1;
    }
    
    private List<DashboardResponse.ScoreTrend> buildScoreTrend(List<Interview> interviews) {
        Map<LocalDate, List<Double>> scoresByDate = interviews.stream()
                .filter(i -> (i.getCompletedAt() != null || i.getCreatedAt() != null))
                .collect(Collectors.groupingBy(
                        i -> (i.getCompletedAt() != null ? i.getCompletedAt() : i.getCreatedAt()).toLocalDate(),
                        TreeMap::new,
                        Collectors.mapping(i -> i.getOverallScore() != null ? i.getOverallScore() : 0.0, Collectors.toList())
                ));
        
        return scoresByDate.entrySet().stream()
                .map(entry -> {
                    DashboardResponse.ScoreTrend trend = new DashboardResponse.ScoreTrend();
                    trend.setDate(entry.getKey().format(DateTimeFormatter.ISO_LOCAL_DATE));
                    trend.setScore(entry.getValue().stream()
                            .mapToDouble(Double::doubleValue)
                            .average()
                            .orElse(0.0));
                    trend.setLabel(entry.getKey().format(DateTimeFormatter.ofPattern("MMM d")));
                    return trend;
                })
                .collect(Collectors.toList());
    }
    
    private DashboardResponse.TopSkill createTopSkill(String name, Double score, String level, Integer rank) {
        DashboardResponse.TopSkill skill = new DashboardResponse.TopSkill();
        skill.setName(name);
        skill.setScore(score);
        skill.setLevel(level);
        skill.setRank(rank);
        return skill;
    }
    
    private DashboardResponse.ImprovingSkill createImprovingSkill(String name, Double rate, String trend) {
        DashboardResponse.ImprovingSkill skill = new DashboardResponse.ImprovingSkill();
        skill.setName(name);
        skill.setImprovementRate(rate);
        skill.setTrend(trend);
        return skill;
    }
    
    private DashboardResponse.WeakSkill createWeakSkill(String name, Double score, String action) {
        DashboardResponse.WeakSkill skill = new DashboardResponse.WeakSkill();
        skill.setName(name);
        skill.setScore(score);
        skill.setRecommendedAction(action);
        return skill;
    }
    
    private String getDailyTip() {
        List<String> tips = Arrays.asList(
                "Practice explaining your thought process out loud during problem-solving",
                "Review one data structure thoroughly each day",
                "Prepare 3-5 stories for behavioral questions using STAR method",
                "Time yourself when solving problems to improve speed",
                "Review your weak areas before starting new topics"
        );
        return tips.get(new Random().nextInt(tips.size()));
    }
    
    private String getMotivationalQuote() {
        List<String> quotes = Arrays.asList(
                "Every expert was once a beginner. Keep practicing!",
                "Success is not final, failure is not fatal. Keep going!",
                "The only way to do great work is to love what you do.",
                "Practice makes progress, not perfection.",
                "Your limitation—it's only your imagination."
        );
        return quotes.get(new Random().nextInt(quotes.size()));
    }
    
    private Integer calculateWeeklyProgress(User user) {
        LocalDateTime weekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY).atStartOfDay();
        List<Interview> weekInterviews = interviewRepository.findCompletedInterviewsBetween(
                user, weekStart, LocalDateTime.now());
        return Math.min(100, (weekInterviews.size() * 100) / 5);
    }
    
    private Integer calculatePointsToNextLevel(User user) {
        // Mock calculation - would be based on actual leveling system
        return 250;
    }
}