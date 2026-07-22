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
    @Cacheable(value = "dashboards", key = "#userId")
    public DashboardResponse getDashboard(java.util.UUID userId) {
        return buildDashboard(userId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public DashboardResponse refreshDashboard(java.util.UUID userId) {
        // This would typically evict cache and rebuild
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
        long totalInterviews = interviewRepository.countByUserAndStatus(user, null);
        stats.setTotalInterviews((int) totalInterviews);
        
        // Completed this month
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        List<Interview> monthInterviews = interviewRepository.findCompletedInterviewsBetween(
                user, monthStart, LocalDateTime.now());
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
        stats.setPreferredDifficulty("MEDIUM"); // Would calculate from actual data
        
        return stats;
    }
    
    private DashboardResponse.PerformanceOverview buildPerformanceOverview(User user) {
        DashboardResponse.PerformanceOverview overview = new DashboardResponse.PerformanceOverview();
        
        // Get recent interviews
        List<Interview> recentInterviews = interviewRepository.findCompletedInterviewsBetween(
                user,
                LocalDateTime.now().minusDays(30),
                LocalDateTime.now()
        );
        
        if (!recentInterviews.isEmpty()) {
            // Current score (last interview)
            Interview lastInterview = recentInterviews.stream()
                    .filter(i -> i.getOverallScore() != null)
                    .max(Comparator.comparing(Interview::getCompletedAt))
                    .orElse(null);
            
            if (lastInterview != null) {
                overview.setCurrentScore(lastInterview.getOverallScore());
                
                // Previous score
                Interview previousInterview = recentInterviews.stream()
                        .filter(i -> i.getOverallScore() != null && 
                               !i.getId().equals(lastInterview.getId()))
                        .max(Comparator.comparing(Interview::getCompletedAt))
                        .orElse(null);
                
                if (previousInterview != null) {
                    overview.setPreviousScore(previousInterview.getOverallScore());
                    overview.setImprovement(overview.getCurrentScore() - overview.getPreviousScore());
                    overview.setTrend(overview.getImprovement() > 0 ? "UP" : 
                                     overview.getImprovement() < 0 ? "DOWN" : "STABLE");
                }
            }
            
            // Average score
            double avgScore = recentInterviews.stream()
                    .filter(i -> i.getOverallScore() != null)
                    .mapToDouble(Interview::getOverallScore)
                    .average()
                    .orElse(0.0);
            overview.setAverageScore(avgScore);
            
            // Best score
            recentInterviews.stream()
                    .filter(i -> i.getOverallScore() != null)
                    .max(Comparator.comparing(Interview::getOverallScore))
                    .ifPresent(best -> {
                        overview.setBestScore(best.getOverallScore());
                        overview.setBestScoreRole(best.getRole());
                    });
            
            // Last 30 days trend
            overview.setLast30DaysTrend(buildScoreTrend(recentInterviews));
        } else {
            overview.setCurrentScore(0.0);
            overview.setTrend("NO_DATA");
            overview.setLast30DaysTrend(new ArrayList<>());
        }
        
        return overview;
    }
    
    private List<DashboardResponse.RecentActivity> buildRecentActivities(User user) {
        List<DashboardResponse.RecentActivity> activities = new ArrayList<>();
        
        // Get recent completed interviews
        List<Interview> recentInterviews = interviewRepository.findByUserAndStatus(
                user, InterviewStatus.COMPLETED, PageRequest.of(0, 5)).getContent();
        
        for (Interview interview : recentInterviews) {
            DashboardResponse.RecentActivity activity = new DashboardResponse.RecentActivity();
            activity.setType("INTERVIEW_COMPLETED");
            activity.setTitle("Completed " + interview.getRole() + " interview");
            activity.setDescription(String.format("Score: %.1f%% | %s difficulty", 
                    interview.getOverallScore(), interview.getDifficultyLevel()));
            activity.setTimestamp(interview.getCompletedAt());
            activity.setIcon("check-circle");
            activity.setLink("/interviews/" + interview.getSessionId());
            activities.add(activity);
        }
        
        // Add skill improvements (mock data for now)
        if (!activities.isEmpty()) {
            DashboardResponse.RecentActivity skillActivity = new DashboardResponse.RecentActivity();
            skillActivity.setType("SKILL_IMPROVED");
            skillActivity.setTitle("Improved in Problem Solving");
            skillActivity.setDescription("+15% improvement in last 5 interviews");
            skillActivity.setTimestamp(LocalDateTime.now().minusDays(2));
            skillActivity.setIcon("trending-up");
            skillActivity.setLink("/analytics/skills");
            activities.add(skillActivity);
        }
        
        // Sort by timestamp
        activities.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        
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
                user, InterviewStatus.COMPLETED, null).getContent();
        
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
        List<Interview> allInterviews = interviewRepository.findByUser(user, null).getContent();
        long totalMinutes = allInterviews.stream()
                .filter(i -> i.getActualDuration() != null)
                .mapToLong(i -> i.getActualDuration().toMinutes())
                .sum();
        return (int) (totalMinutes / 60);
    }
    
    private Integer calculateCurrentStreak(User user) {
        List<Interview> recentInterviews = interviewRepository.findByUserAndStatus(
                user, InterviewStatus.COMPLETED, null).getContent().stream()
                .filter(i -> i.getCompletedAt() != null)
                .sorted(Comparator.comparing(Interview::getCompletedAt).reversed())
                .collect(Collectors.toList());
        
        if (recentInterviews.isEmpty()) return 0;
        
        int streak = 1;
        LocalDate lastDate = recentInterviews.get(0).getCompletedAt().toLocalDate();
        
        // Check if streak is still active
        if (ChronoUnit.DAYS.between(lastDate, LocalDate.now()) > 1) {
            return 0;
        }
        
        for (int i = 1; i < recentInterviews.size(); i++) {
            LocalDate currentDate = recentInterviews.get(i).getCompletedAt().toLocalDate();
            if (ChronoUnit.DAYS.between(currentDate, lastDate) == 1) {
                streak++;
                lastDate = currentDate;
            } else {
                break;
            }
        }
        
        return streak;
    }
    
    private List<DashboardResponse.ScoreTrend> buildScoreTrend(List<Interview> interviews) {
        Map<LocalDate, List<Double>> scoresByDate = interviews.stream()
                .filter(i -> i.getOverallScore() != null && i.getCompletedAt() != null)
                .collect(Collectors.groupingBy(
                        i -> i.getCompletedAt().toLocalDate(),
                        TreeMap::new,
                        Collectors.mapping(Interview::getOverallScore, Collectors.toList())
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