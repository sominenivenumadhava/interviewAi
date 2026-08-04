package com.interviai.backend.module.interview.generation.impl;

import com.interviai.backend.module.interview.enums.InterviewType;
import com.interviai.backend.module.interview.generation.AbstractQuestionGenerator;
import com.interviai.backend.module.interview.generation.FallbackQuestionBank;
import com.interviai.backend.module.interview.generation.GeneratedQuestionDraft;
import com.interviai.backend.module.interview.generation.QuestionGenerationContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ManagerialQuestionGenerator extends AbstractQuestionGenerator {

    @Override
    public InterviewType supportedType() {
        return InterviewType.MANAGERIAL;
    }

    @Override
    public List<String> defaultEvaluationCriteria() {
        return List.of("Decision Making", "Ownership", "Leadership", "Stakeholder Management");
    }

    @Override
    public boolean isValidCategory(String category) {
        return categoryMatches(category, "managerial", "bar raiser", "leadership", "management");
    }

    @Override
    public String buildSystemPrompt(QuestionGenerationContext ctx) {
        return """
            You are a Bar Raiser / hiring manager interviewer at %s for a %s candidate (%s).
            Session ID: %s — invent new leadership scenarios; never repeat the same story prompt.
            
            STRICT RULES — MANAGERIAL / BAR RAISER ROUND ONLY:
            - Generate ONLY scenario-based leadership questions. Randomize across:
              ownership, leadership, prioritization, conflict resolution, stakeholder management,
              customer obsession, architecture decisions, project failure, mentoring, decision making.
            - FORBIDDEN: DSA coding problems, LeetCode, pure technical trivia drills, basic HR screening only.
            - Every question category MUST be exactly "Managerial".
            - Generate completely new questions different from previously generated ones.
              Avoid semantic duplication and repeated wording. Change scenarios every time.
            
            Return raw JSON only:
            {
              "questions": [
                {
                  "question": "...",
                  "category": "Managerial",
                  "difficulty": "%s",
                  "expectedTimeMinutes": 6,
                  "evaluationCriteria": ["Decision Making", "Ownership", "Leadership", "Stakeholder Management"],
                  "followUpQuestions": ["..."]
                }
              ]
            }
            """.formatted(
                ctx.safeCompany(),
                ctx.safeRole(),
                ctx.safeExperience(),
                ctx.safeSessionId(),
                ctx.safeDifficulty()
        );
    }

    @Override
    public String buildUserPrompt(QuestionGenerationContext ctx) {
        return """
            Generate exactly %d distinct MANAGERIAL / Bar Raiser scenario questions.
            Never reuse identical scenarios from the avoid-list.
            
            %s
            """.formatted(ctx.getQuestionCount(), commonContextBlock(ctx));
    }

    @Override
    public List<GeneratedQuestionDraft> fallbackQuestions(QuestionGenerationContext ctx) {
        return FallbackQuestionBank.managerial(ctx);
    }
}
