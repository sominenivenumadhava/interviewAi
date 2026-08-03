package com.interviai.backend.module.interview.generation.impl;

import com.interviai.backend.module.interview.enums.InterviewType;
import com.interviai.backend.module.interview.generation.AbstractQuestionGenerator;
import com.interviai.backend.module.interview.generation.FallbackQuestionBank;
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
            You are an HR / recruiter interviewer at %s conducting an HR screening round for a %s candidate (%s experience).
            Session ID: %s — treat this as a brand-new interview; do not reuse stock questions from prior sessions.
            
            STRICT RULES — HR ROUND ONLY:
            - Generate ONLY HR / screening / soft-skill / behavioral questions.
            - Randomize themes across: leadership, conflict, teamwork, failure, strengths, weaknesses,
              career goals, decision making, culture fit, motivation, ownership, feedback, adaptability.
            - NEVER open every interview with the same "Tell me about yourself" unless the avoid-list is empty
              and you still rephrase uniquely for this company and role.
            - FORBIDDEN: coding problems, DSA, system design, deep technical theory.
            - Every question category MUST be exactly "HR".
            - Generate a completely new question set different from previously generated questions.
              Avoid semantic duplication and repeated wording.
            
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
            Generate exactly %d distinct HR round interview questions for question numbers 1..%d.
            Randomize scenarios so this session does not resemble a prior interview.
            
            %s
            """.formatted(ctx.getQuestionCount(), ctx.getQuestionCount(), commonContextBlock(ctx));
    }

    @Override
    public List<GeneratedQuestionDraft> fallbackQuestions(QuestionGenerationContext ctx) {
        return FallbackQuestionBank.hr(ctx);
    }
}
