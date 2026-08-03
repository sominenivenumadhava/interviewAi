package com.interviai.backend.module.interview.generation.impl;

import com.interviai.backend.module.interview.enums.InterviewType;
import com.interviai.backend.module.interview.generation.AbstractQuestionGenerator;
import com.interviai.backend.module.interview.generation.GeneratedQuestionDraft;
import com.interviai.backend.module.interview.generation.QuestionGenerationContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HrQuestionGenerator extends AbstractQuestionGenerator {

    @Override
    public InterviewType supportedType() {
        return InterviewType.HR;
    }

    @Override
    public List<String> defaultEvaluationCriteria() {
        return List.of("Communication", "Confidence", "Personality", "Cultural Fit");
    }

    @Override
    public boolean isValidCategory(String category) {
        return categoryMatches(category, "hr", "behavioral", "screening", "culture");
    }

    @Override
    public String buildSystemPrompt(QuestionGenerationContext ctx) {
        return """
            You are an HR / recruiter interviewer at %s conducting an HR screening round for a %s candidate.
            
            STRICT RULES — HR ROUND ONLY:
            - Generate ONLY HR / screening / soft-skill questions.
            - Allowed themes: Tell me about yourself, Why should we hire you, strengths/weaknesses,
              Why this company, 5-year plan, challenges faced, leadership, teamwork, conflict resolution,
              motivation, salary/availability (tastefully), culture fit, STAR behavioral stories.
            - FORBIDDEN: coding problems, DSA, system design, deep technical theory, LeetCode-style questions.
            - Every question category MUST be exactly "HR".
            
            Return raw JSON only (no markdown):
            {
              "questions": [
                {
                  "question": "...",
                  "category": "HR",
                  "difficulty": "%s",
                  "expectedTimeMinutes": 4,
                  "evaluationCriteria": ["Communication", "Confidence", "Personality", "Cultural Fit"],
                  "followUpQuestions": ["..."]
                }
              ]
            }
            """.formatted(ctx.safeCompany(), ctx.safeRole(), ctx.safeDifficulty());
    }

    @Override
    public String buildUserPrompt(QuestionGenerationContext ctx) {
        return """
            Generate exactly %d distinct HR round interview questions.
            
            %s
            
            Do not repeat the same question. Tailor wording to the company and role.
            """.formatted(ctx.getQuestionCount(), commonContextBlock(ctx));
    }

    @Override
    public List<GeneratedQuestionDraft> fallbackQuestions(QuestionGenerationContext ctx) {
        String role = ctx.safeRole();
        String company = ctx.safeCompany();
        return List.of(
                baseDraft("Tell me about yourself and walk me through your career journey relevant to the " + role + " role.",
                        ctx, List.of("What accomplishment are you most proud of?")),
                baseDraft("Why do you want to join " + company + " as a " + role + "?",
                        ctx, List.of("What do you know about our products or culture?")),
                baseDraft("What are your greatest strengths and one area you are actively improving?",
                        ctx, List.of("How has that improvement shown up at work recently?")),
                baseDraft("Why should we hire you for this " + role + " position?",
                        ctx, List.of("What unique value would you bring in the first 90 days?")),
                baseDraft("Where do you see yourself in 5 years, and how does this role fit that path?",
                        ctx, List.of("What skills do you want to develop here?")),
                baseDraft("Describe a challenge you faced at work and how you handled it (use STAR).",
                        ctx, List.of("What would you do differently next time?")),
                baseDraft("Tell me about a time you had a conflict with a teammate and how you resolved it.",
                        ctx, List.of("How did the working relationship change afterward?")),
                baseDraft("Describe a situation where you demonstrated leadership or ownership without a formal title.",
                        ctx, List.of("What was the measurable outcome?"))
        );
    }
}
