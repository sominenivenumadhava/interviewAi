package com.interviai.backend.module.interview.generation;

import com.interviai.backend.module.interview.enums.DifficultyLevel;
import com.interviai.backend.module.interview.enums.InterviewType;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestionGenerationVarietyTest {

    @Test
    void differentSessionSeedsProduceDifferentFirstFallbackQuestions() {
        Set<String> firstQuestions = new HashSet<>();
        for (int i = 0; i < 12; i++) {
            QuestionGenerationContext ctx = QuestionGenerationContext.builder()
                    .interviewType(InterviewType.CODING)
                    .role("Backend Engineer")
                    .company("Google")
                    .difficulty(DifficultyLevel.MEDIUM)
                    .questionCount(5)
                    .sessionId("session-" + i)
                    .sessionSeed(QuestionUniquenessHelper.seedFrom("session-" + i, "nonce-" + i))
                    .diversityNonce("nonce-" + i)
                    .build();
            List<GeneratedQuestionDraft> bank = FallbackQuestionBank.coding(ctx);
            assertFalse(bank.isEmpty());
            firstQuestions.add(bank.get(0).getQuestionText());
        }
        // Across 12 seeds we should see multiple distinct opening problems
        assertTrue(firstQuestions.size() >= 4,
                "Expected diverse first coding questions, got " + firstQuestions.size());
    }

    @Test
    void similarityDetectsNearDuplicates() {
        assertTrue(QuestionUniquenessHelper.isTooSimilar(
                "Design a URL Shortener like bit.ly",
                List.of("Design a URL shortener (like bit.ly). Cover hashing and caching."),
                0.72
        ));
        assertFalse(QuestionUniquenessHelper.isTooSimilar(
                "Design a food delivery matching platform with ETA",
                List.of("Design a URL shortener like bit.ly"),
                0.72
        ));
    }

    @Test
    void aptitudeNumbersVaryBySeed() {
        QuestionGenerationContext a = base(InterviewType.APTITUDE, "a", 111L);
        QuestionGenerationContext b = base(InterviewType.APTITUDE, "b", 999L);
        String qa = FallbackQuestionBank.aptitude(a).get(0).getQuestionText();
        String qb = FallbackQuestionBank.aptitude(b).get(0).getQuestionText();
        // Different seed should usually change order and/or numbers
        assertNotEquals(qa, qb);
    }

    private QuestionGenerationContext base(InterviewType type, String session, long seed) {
        return QuestionGenerationContext.builder()
                .interviewType(type)
                .role("SDE")
                .company("Amazon")
                .difficulty(DifficultyLevel.MEDIUM)
                .questionCount(5)
                .sessionId(session)
                .sessionSeed(seed)
                .diversityNonce(session)
                .build();
    }
}
