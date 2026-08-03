package com.interviai.backend.module.interview.generation.impl;

import com.interviai.backend.module.interview.enums.InterviewType;
import com.interviai.backend.module.interview.generation.AbstractQuestionGenerator;
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
            You are conducting an aptitude / assessment round for a %s candidate at %s.
            
            STRICT RULES — APTITUDE ROUND ONLY:
            - Generate ONLY aptitude questions from: Quantitative, Logical Reasoning, Verbal Ability,
              Data Interpretation, and Puzzles.
            - Each question should be self-contained with a clear problem; include the expected approach in follow-ups.
            - FORBIDDEN: HR questions, coding/DSA problems, system design, deep technical theory.
            - Every question category MUST be exactly "Aptitude".
            
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
            """.formatted(ctx.safeRole(), ctx.safeCompany(), ctx.safeDifficulty());
    }

    @Override
    public String buildUserPrompt(QuestionGenerationContext ctx) {
        return """
            Generate exactly %d distinct APTITUDE assessment questions mixed across quantitative, logical, verbal, DI, and puzzles.
            
            %s
            """.formatted(ctx.getQuestionCount(), commonContextBlock(ctx));
    }

    @Override
    public List<GeneratedQuestionDraft> fallbackQuestions(QuestionGenerationContext ctx) {
        return List.of(
                baseDraft("A train 120 m long passes a pole in 6 seconds. What is its speed in km/h?",
                        ctx, List.of("Show the conversion from m/s to km/h.")),
                baseDraft("If the ratio of ages of A and B is 3:5 and B is 20 years older than A, what are their ages?",
                        ctx, List.of("Write the equations you used.")),
                baseDraft("Find the next number in the series: 2, 6, 12, 20, 30, ?",
                        ctx, List.of("What pattern did you identify?")),
                baseDraft("In a certain code, COMPUTER is written as RFUVQNPC. How is MEDICINE written in that code?",
                        ctx, List.of("Explain the letter transformation rule.")),
                baseDraft("A pie chart shows company expenses: Salaries 40%, Rent 20%, Marketing 15%, Ops 15%, Misc 10%. If total expense is $2,00,000, what is spent on Marketing?",
                        ctx, List.of("What percentage remains for Salaries + Rent?")),
                baseDraft("You have 8 identical-looking balls; one is heavier. Using a balance scale, what is the minimum weighings in the worst case to find the heavy ball?",
                        ctx, List.of("Generalize to 9 balls.")),
                baseDraft("Two pipes fill a tank in 12 and 15 hours respectively. A drain empties it in 20 hours. If all three are opened, how long to fill the tank?",
                        ctx, List.of("Write the combined work-rate equation.")),
                baseDraft("Choose the odd one out: Square, Rectangle, Circle, Triangle — and justify why.",
                        ctx, List.of("Give an alternative grouping that yields a different odd one."))
        );
    }
}
