package com.interviai.backend.module.interview.generation.impl;

import com.interviai.backend.module.interview.enums.InterviewType;
import com.interviai.backend.module.interview.generation.AbstractQuestionGenerator;
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
            You are a Bar Raiser / hiring manager interviewer at %s for a %s candidate.
            
            STRICT RULES — MANAGERIAL / BAR RAISER ROUND ONLY:
            - Generate ONLY scenario-based leadership questions: decision making, ownership, stakeholder management,
              conflict resolution, project failures, architecture decisions (as leadership choices — not DSA),
              mentoring juniors, prioritization, customer focus.
            - FORBIDDEN: DSA coding problems, LeetCode, pure technical trivia drills, basic HR screening only.
            - Every question category MUST be exactly "Managerial".
            
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
            """.formatted(ctx.safeCompany(), ctx.safeRole(), ctx.safeDifficulty());
    }

    @Override
    public String buildUserPrompt(QuestionGenerationContext ctx) {
        return """
            Generate exactly %d distinct MANAGERIAL / Bar Raiser scenario questions.
            
            %s
            """.formatted(ctx.getQuestionCount(), commonContextBlock(ctx));
    }

    @Override
    public List<GeneratedQuestionDraft> fallbackQuestions(QuestionGenerationContext ctx) {
        String role = ctx.safeRole();
        return List.of(
                baseDraft("Describe a high-stakes decision you made as a " + role + " with incomplete information. How did you decide and what was the outcome?",
                        ctx, List.of("What signals told you it was time to commit?")),
                baseDraft("Tell me about a time you took ownership of a failing project. What changed under your leadership?",
                        ctx, List.of("How did you reset stakeholder expectations?")),
                baseDraft("Describe a conflict between engineering priorities and business deadlines. How did you resolve it?",
                        ctx, List.of("What trade-off did you explicitly accept?")),
                baseDraft("Share an example of mentoring a junior engineer who was struggling. What was your approach?",
                        ctx, List.of("How did you measure improvement?")),
                baseDraft("Tell me about a project failure. What was your role, what did you learn, and what process changed afterward?",
                        ctx, List.of("How did you communicate the failure to leadership?")),
                baseDraft("How do you prioritize a backlog when everything is marked P0 by different stakeholders?",
                        ctx, List.of("Give a concrete example with conflicting customers.")),
                baseDraft("Describe an architecture or technology decision you influenced. How did you evaluate trade-offs and get buy-in?",
                        ctx, List.of("What would you revisit with today's information?")),
                baseDraft("Tell me about a time you put the customer first even when it was inconvenient for the team.",
                        ctx, List.of("How did you balance quality with speed?"))
        );
    }
}
