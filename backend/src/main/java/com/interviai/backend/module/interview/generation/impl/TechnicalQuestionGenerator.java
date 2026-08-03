package com.interviai.backend.module.interview.generation.impl;

import com.interviai.backend.module.interview.enums.InterviewType;
import com.interviai.backend.module.interview.generation.AbstractQuestionGenerator;
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
            You are a senior technical interviewer at %s hiring a %s.
            
            STRICT RULES — TECHNICAL THEORY ROUND ONLY:
            - Generate ONLY technical theory / concept questions grounded in the candidate's skills, role, and company stack.
            - Topics may include: OOP, Collections, Multithreading, REST APIs, Microservices, DBMS, OS, CN,
              Java/Spring Boot, JavaScript/React, Cloud, Docker, Kubernetes, caching, testing — as relevant to skills.
            - FORBIDDEN: "Tell me about yourself", career background, HR motivation, LeetCode coding problems,
              full system-design case studies (those belong to other rounds).
            - Every question category MUST be exactly "Technical".
            
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
            """.formatted(ctx.safeCompany(), ctx.safeRole(), ctx.safeDifficulty());
    }

    @Override
    public String buildUserPrompt(QuestionGenerationContext ctx) {
        return """
            Generate exactly %d distinct TECHNICAL theory interview questions.
            Prefer topics from the candidate skills list. Match difficulty %s.
            
            %s
            """.formatted(ctx.getQuestionCount(), ctx.safeDifficulty(), commonContextBlock(ctx));
    }

    @Override
    public List<GeneratedQuestionDraft> fallbackQuestions(QuestionGenerationContext ctx) {
        String role = ctx.safeRole();
        return List.of(
                baseDraft("Explain the core OOP principles and how you apply them in day-to-day " + role + " work.",
                        ctx, List.of("Give a concrete example of polymorphism from a project.")),
                baseDraft("Compare SQL vs NoSQL databases. When would you choose each for a " + role + " service?",
                        ctx, List.of("How do indexes affect query performance?")),
                baseDraft("Explain REST API design best practices: status codes, idempotency, and versioning.",
                        ctx, List.of("How do you handle partial failures in distributed calls?")),
                baseDraft("What is the difference between process and thread? How do you avoid race conditions?",
                        ctx, List.of("Explain a concurrency bug you have debugged.")),
                baseDraft("Explain how you would design authentication and authorization for a microservice used by a " + role + ".",
                        ctx, List.of("JWT vs session cookies — trade-offs?")),
                baseDraft("Describe the JVM memory model / garbage collection basics (or equivalent runtime for your stack).",
                        ctx, List.of("How do you diagnose a memory leak?")),
                baseDraft("Explain CAP theorem and consistency models relevant to distributed systems.",
                        ctx, List.of("Where have you accepted eventual consistency?")),
                baseDraft("What testing pyramid do you follow (unit/integration/e2e) and why?",
                        ctx, List.of("How do you test async or event-driven code?"))
        );
    }
}
