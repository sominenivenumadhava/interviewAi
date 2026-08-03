package com.interviai.backend.module.interview.generation.impl;

import com.interviai.backend.module.interview.enums.InterviewType;
import com.interviai.backend.module.interview.generation.AbstractQuestionGenerator;
import com.interviai.backend.module.interview.generation.FallbackQuestionBank;
import com.interviai.backend.module.interview.generation.GeneratedQuestionDraft;
import com.interviai.backend.module.interview.generation.QuestionGenerationContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SystemDesignQuestionGenerator extends AbstractQuestionGenerator {

    @Override
    public InterviewType supportedType() {
        return InterviewType.SYSTEM_DESIGN;
    }

    @Override
    public List<String> defaultEvaluationCriteria() {
        return List.of("Architecture", "Scalability", "Trade-offs", "Communication");
    }

    @Override
    public boolean isValidCategory(String category) {
        return categoryMatches(category, "system design", "architecture", "hld", "lld");
    }

    @Override
    protected int defaultExpectedMinutes() {
        return 20;
    }

    @Override
    public String buildSystemPrompt(QuestionGenerationContext ctx) {
        return """
            You are a system design interviewer at %s hiring a %s (%s).
            Session ID: %s — choose a fresh design prompt; do NOT default to URL Shortener every time.
            
            STRICT RULES — SYSTEM DESIGN ROUND ONLY:
            - Generate ONLY system design prompts. Randomize among domains such as:
              URL Shortener, Instagram, WhatsApp, Uber, YouTube, Google Drive, Notification System,
              Food Delivery, Chat Application, Banking System, Payment Gateway, Inventory System,
              Rate Limiter, Job Scheduler, Autocomplete, Collaborative Docs, Observability — pick different ones each session.
            - Discuss different scalability challenges every interview.
            - FORBIDDEN: DSA coding problems, HR questions, pure CS trivia without a design prompt.
            - Every question category MUST be exactly "System Design".
            - Generate completely new prompts different from previously generated questions.
              Avoid semantic duplication and repeated wording.
            
            Return raw JSON only:
            {
              "questions": [
                {
                  "question": "Design ... Cover requirements, APIs, data model, scaling...",
                  "category": "System Design",
                  "difficulty": "%s",
                  "expectedTimeMinutes": 20,
                  "evaluationCriteria": ["Architecture", "Scalability", "Trade-offs", "Communication"],
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
            Generate exactly %d distinct SYSTEM DESIGN interview prompts tailored to %s at %s.
            Randomize the product domain; do not reuse the avoid-list systems.
            
            %s
            """.formatted(ctx.getQuestionCount(), ctx.safeRole(), ctx.safeCompany(), commonContextBlock(ctx));
    }

    @Override
    public List<GeneratedQuestionDraft> fallbackQuestions(QuestionGenerationContext ctx) {
        return FallbackQuestionBank.systemDesign(ctx);
    }
}
