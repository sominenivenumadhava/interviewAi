package com.interviai.backend.module.interview.generation.impl;

import com.interviai.backend.module.interview.enums.InterviewType;
import com.interviai.backend.module.interview.generation.AbstractQuestionGenerator;
import com.interviai.backend.module.interview.generation.FallbackQuestionBank;
import com.interviai.backend.module.interview.generation.GeneratedQuestionDraft;
import com.interviai.backend.module.interview.generation.QuestionGenerationContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TechnicalQuestionGenerator extends AbstractQuestionGenerator {

    @Override
    public InterviewType supportedType() {
        return InterviewType.TECHNICAL;
    }

    @Override
    public List<String> defaultEvaluationCriteria() {
        return List.of("Core Concepts", "Practical Knowledge", "Problem Solving", "Confidence");
    }

    @Override
    public boolean isValidCategory(String category) {
        return categoryMatches(category, "technical", "theory", "cs concepts");
    }

    @Override
    public String buildSystemPrompt(QuestionGenerationContext ctx) {
        return """
            You are a senior technical interviewer at %s hiring a %s (%s).
            Session ID: %s — invent a fresh technical question set; do not recycle classic openers.
            
            STRICT RULES — TECHNICAL THEORY ROUND ONLY:
            - Generate ONLY technical theory / concept questions grounded in skills, role, and company stack.
            - Randomize across topics as relevant: Java, Spring Boot, React, JavaScript, Node.js, Python,
              DBMS, OS, CN, OOP, REST APIs, Microservices, Docker, Kubernetes, AWS, SQL, Git,
              Authentication, Caching, Security — pick a diverse mix each session.
            - FORBIDDEN: "Tell me about yourself", HR motivation, LeetCode coding problems,
              full system-design case studies.
            - Every question category MUST be exactly "Technical".
            - Generate completely new questions different from previously generated ones.
              Avoid semantic duplication and repeated wording. Vary examples and numbers.
            
            Return raw JSON only (no markdown):
            {
              "questions": [
                {
                  "question": "...",
                  "category": "Technical",
                  "difficulty": "%s",
                  "expectedTimeMinutes": 5,
                  "evaluationCriteria": ["Core Concepts", "Practical Knowledge", "Problem Solving", "Confidence"],
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
            Generate exactly %d distinct TECHNICAL theory interview questions.
            Prefer topics from the candidate skills list. Match difficulty %s.
            Do not always start with OOP principles — diversify the opening question.
            
            %s
            """.formatted(ctx.getQuestionCount(), ctx.safeDifficulty(), commonContextBlock(ctx));
    }

    @Override
    public List<GeneratedQuestionDraft> fallbackQuestions(QuestionGenerationContext ctx) {
        return FallbackQuestionBank.technical(ctx);
    }
}
