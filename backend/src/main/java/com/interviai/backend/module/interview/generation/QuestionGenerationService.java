package com.interviai.backend.module.interview.generation;

import com.interviai.backend.module.interview.entity.Interview;
import com.interviai.backend.module.interview.entity.InterviewQuestion;
import com.interviai.backend.module.interview.enums.InterviewType;
import com.interviai.backend.module.interview.repository.InterviewQuestionRepository;
import com.interviai.backend.module.interview.repository.InterviewRepository;
import com.interviai.backend.module.user.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Central Question Generation Engine.
 * Ensures every interview session gets a unique, non-repeating question set
 * tailored to company, role, round, difficulty, skills, and experience.
 */
@Service
public class QuestionGenerationService {

    private static final Logger log = LoggerFactory.getLogger(QuestionGenerationService.class);
    private static final double SIMILARITY_THRESHOLD = 0.72;
    private static final int RECENT_SESSION_LOOKBACK = 8;

    private final RoundAwareQuestionGenerator roundAwareQuestionGenerator;
    private final InterviewRepository interviewRepository;
    private final InterviewQuestionRepository questionRepository;

    public QuestionGenerationService(
            RoundAwareQuestionGenerator roundAwareQuestionGenerator,
            InterviewRepository interviewRepository,
            InterviewQuestionRepository questionRepository
    ) {
        this.roundAwareQuestionGenerator = roundAwareQuestionGenerator;
        this.interviewRepository = interviewRepository;
        this.questionRepository = questionRepository;
    }

    /**
     * Generate a fresh question set for an interview session.
     */
    public List<GeneratedQuestionDraft> generateForInterview(Interview interview, QuestionGenerationContext baseContext) {
        String sessionId = interview.getSessionId() != null
                ? interview.getSessionId()
                : UUID.randomUUID().toString();
        String nonce = UUID.randomUUID().toString();
        long seed = QuestionUniquenessHelper.seedFrom(sessionId, nonce);

        List<String> recentAvoid = loadRecentQuestionTexts(interview);
        List<String> askedTopics = recentAvoid.stream()
                .map(QuestionUniquenessHelper::extractTopicHint)
                .distinct()
                .limit(40)
                .collect(Collectors.toCollection(ArrayList::new));

        QuestionGenerationContext ctx = QuestionGenerationContext.builder()
                .interviewType(baseContext.getInterviewType())
                .role(baseContext.getRole())
                .company(baseContext.getCompany())
                .jobDescription(baseContext.getJobDescription())
                .difficulty(baseContext.getDifficulty())
                .questionCount(baseContext.getQuestionCount())
                .focusAreas(baseContext.getFocusAreas())
                .customInstructions(baseContext.getCustomInstructions())
                .resumeContent(baseContext.getResumeContent())
                .candidateSkills(baseContext.getCandidateSkills())
                .preferredLanguage(baseContext.getPreferredLanguage())
                .experienceLevel(baseContext.getExperienceLevel())
                .sessionId(sessionId)
                .sessionSeed(seed)
                .diversityNonce(nonce)
                .askedQuestions(new ArrayList<>(recentAvoid))
                .askedTopics(askedTopics)
                .build();

        log.info("QuestionGenerationService: session={}, round={}, seed={}, avoidPool={}",
                sessionId, ctx.effectiveType(), seed, recentAvoid.size());

        List<GeneratedQuestionDraft> drafts = roundAwareQuestionGenerator.generate(ctx);
        return dedupeAndValidate(drafts, ctx, recentAvoid);
    }

    private List<GeneratedQuestionDraft> dedupeAndValidate(
            List<GeneratedQuestionDraft> drafts,
            QuestionGenerationContext ctx,
            List<String> recentAvoid
    ) {
        List<GeneratedQuestionDraft> unique = new ArrayList<>();
        List<String> acceptedTexts = new ArrayList<>(recentAvoid);
        Set<String> topicCounts = new LinkedHashSet<>();

        for (GeneratedQuestionDraft draft : drafts) {
            if (draft == null || draft.getQuestionText() == null || draft.getQuestionText().isBlank()) {
                continue;
            }
            String text = draft.getQuestionText().trim();
            if (QuestionUniquenessHelper.isTooSimilar(text, acceptedTexts, SIMILARITY_THRESHOLD)) {
                log.debug("Dropping near-duplicate question in session {}", ctx.getSessionId());
                continue;
            }
            String topic = QuestionUniquenessHelper.extractTopicHint(text);
            // Soft topic diversity: allow at most ~2 near-identical topic fingerprints in one set
            long sameTopic = topicCounts.stream().filter(t -> QuestionUniquenessHelper.similarity(t, topic) >= 0.85).count();
            if (sameTopic >= 2 && unique.size() + 1 < ctx.getQuestionCount()) {
                // Prefer variety; still accept if we are short after fill
                continue;
            }
            topicCounts.add(topic);
            acceptedTexts.add(text);
            unique.add(draft);
        }

        if (unique.size() < ctx.getQuestionCount()) {
            // Request a second creative pass with updated avoid-list
            QuestionGenerationContext retryCtx = QuestionGenerationContext.builder()
                    .interviewType(ctx.getInterviewType())
                    .role(ctx.getRole())
                    .company(ctx.getCompany())
                    .jobDescription(ctx.getJobDescription())
                    .difficulty(ctx.getDifficulty())
                    .questionCount(ctx.getQuestionCount() - unique.size())
                    .focusAreas(ctx.getFocusAreas())
                    .customInstructions(ctx.getCustomInstructions())
                    .resumeContent(ctx.getResumeContent())
                    .candidateSkills(ctx.getCandidateSkills())
                    .preferredLanguage(ctx.getPreferredLanguage())
                    .experienceLevel(ctx.getExperienceLevel())
                    .sessionId(ctx.getSessionId())
                    .sessionSeed(QuestionUniquenessHelper.seedFrom(ctx.getSessionId(), UUID.randomUUID().toString()))
                    .diversityNonce(UUID.randomUUID().toString())
                    .askedQuestions(new ArrayList<>(acceptedTexts))
                    .askedTopics(new ArrayList<>(topicCounts))
                    .build();

            List<GeneratedQuestionDraft> more = roundAwareQuestionGenerator.generate(retryCtx);
            for (GeneratedQuestionDraft draft : more) {
                if (draft == null || draft.getQuestionText() == null || draft.getQuestionText().isBlank()) {
                    continue;
                }
                if (QuestionUniquenessHelper.isTooSimilar(draft.getQuestionText(), acceptedTexts, SIMILARITY_THRESHOLD)) {
                    continue;
                }
                acceptedTexts.add(draft.getQuestionText());
                unique.add(draft);
                if (unique.size() >= ctx.getQuestionCount()) {
                    break;
                }
            }
        }

        return unique.isEmpty() ? drafts : unique.subList(0, Math.min(ctx.getQuestionCount(), unique.size()));
    }

    /**
     * Pull question texts from recent similar interviews so new sessions diverge.
     */
    private List<String> loadRecentQuestionTexts(Interview interview) {
        Set<String> texts = new LinkedHashSet<>();
        try {
            User user = interview.getUser();
            if (user == null) {
                return new ArrayList<>();
            }
            List<Interview> recent = interviewRepository.findByUserOrderByCreatedAtDesc(user);
            InterviewType targetType = interview.getInterviewType() != null
                    ? interview.getInterviewType().canonicalize()
                    : null;
            int sessions = 0;
            for (Interview past : recent) {
                if (past.getSessionId() != null && past.getSessionId().equals(interview.getSessionId())) {
                    continue;
                }
                if (targetType != null && past.getInterviewType() != null
                        && past.getInterviewType().canonicalize() != targetType) {
                    continue;
                }
                // Prefer same company/role when available, but still learn from other rounds of same type
                List<InterviewQuestion> qs = questionRepository.findByInterviewOrderByQuestionOrder(past);
                for (InterviewQuestion q : qs) {
                    if (q.getQuestionText() != null && !q.getQuestionText().isBlank()) {
                        texts.add(q.getQuestionText().trim());
                    }
                }
                sessions++;
                if (sessions >= RECENT_SESSION_LOOKBACK || texts.size() >= 80) {
                    break;
                }
            }
        } catch (Exception e) {
            log.warn("Could not load recent questions for diversity: {}", e.getMessage());
        }
        return new ArrayList<>(texts);
    }
}
