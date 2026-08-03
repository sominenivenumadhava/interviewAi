package com.interviai.backend.module.interview.generation.impl;

import com.interviai.backend.module.interview.enums.InterviewType;
import com.interviai.backend.module.interview.generation.AbstractQuestionGenerator;
import com.interviai.backend.module.interview.generation.FallbackQuestionBank;
import com.interviai.backend.module.interview.generation.GeneratedQuestionDraft;
import com.interviai.backend.module.interview.generation.QuestionGenerationContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AptitudeQuestionGenerator extends AbstractQuestionGenerator {

    @Override
    public InterviewType supportedType() {
        return InterviewType.APTITUDE;
    }

    @Override
    public List<String> defaultEvaluationCriteria() {
        return List.of("Accuracy", "Logical Reasoning", "Speed", "Clarity of Approach");
    }

    @Override
    public boolean isValidCategory(String category) {
        return categoryMatches(category, "aptitude", "quantitative", "logical", "verbal", "puzzle");
    }

    @Override
    public String buildSystemPrompt(QuestionGenerationContext ctx) {
        return """
            You are conducting an aptitude / assessment round for a %s candidate at %s (%s).
            Session ID: %s — invent NEW numbers, names, and scenarios; never reuse the same train/pole problem.
            
            STRICT RULES — APTITUDE ROUND ONLY:
            - Generate ONLY aptitude questions. Randomize across: Quantitative, Logical Reasoning,
              Verbal Ability, Data Interpretation, and Puzzles.
            - Randomize values, names, options, scenarios, and topics every session.
            - FORBIDDEN: HR questions, coding/DSA problems, system design, deep technical theory.
            - Every question category MUST be exactly "Aptitude".
            - Generate completely new questions different from previously generated ones.
              Avoid semantic duplication and repeated wording.
            
            Return raw JSON only:
            {
              "questions": [
                {
                  "question": "...",
                  "category": "Aptitude",
                  "difficulty": "%s",
                  "expectedTimeMinutes": 3,
                  "evaluationCriteria": ["Accuracy", "Logical Reasoning", "Speed", "Clarity of Approach"],
                  "followUpQuestions": ["Explain your approach step by step."]
                }
              ]
            }
            """.formatted(
                ctx.safeRole(),
                ctx.safeCompany(),
                ctx.safeExperience(),
                ctx.safeSessionId(),
                ctx.safeDifficulty()
        );
    }

    @Override
    public String buildUserPrompt(QuestionGenerationContext ctx) {
        return """
            Generate exactly %d distinct APTITUDE assessment questions mixed across quantitative, logical, verbal, DI, and puzzles.
            Change all numeric values relative to any avoid-list questions.
            
            %s
            """.formatted(ctx.getQuestionCount(), commonContextBlock(ctx));
    }

    @Override
    public List<GeneratedQuestionDraft> fallbackQuestions(QuestionGenerationContext ctx) {
        return FallbackQuestionBank.aptitude(ctx);
    }
}
