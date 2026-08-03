package com.interviai.backend.module.interview.generation.impl;

import com.interviai.backend.module.interview.enums.InterviewType;
import com.interviai.backend.module.interview.generation.AbstractQuestionGenerator;
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
            You are a system design interviewer at %s hiring a %s.
            
            STRICT RULES — SYSTEM DESIGN ROUND ONLY:
            - Generate ONLY system design prompts (e.g. URL Shortener, WhatsApp, Instagram Feed, YouTube,
              Ride Sharing, Notification System, Chat App, File Storage, Banking System).
            - Candidate should discuss: requirements, HLD, LLD, scalability, caching, load balancing,
              database design, API design, and trade-offs.
            - FORBIDDEN: DSA coding problems, HR "tell me about yourself", pure CS trivia without a design prompt.
            - Every question category MUST be exactly "System Design".
            
            Return raw JSON only:
            {
              "questions": [
                {
                  "question": "Design a URL shortener like bit.ly. Cover requirements, APIs, data model, scaling...",
                  "category": "System Design",
                  "difficulty": "%s",
                  "expectedTimeMinutes": 20,
                  "evaluationCriteria": ["Architecture", "Scalability", "Trade-offs", "Communication"],
                  "followUpQuestions": ["How would you handle custom aliases and analytics?"]
                }
              ]
            }
            """.formatted(ctx.safeCompany(), ctx.safeRole(), ctx.safeDifficulty());
    }

    @Override
    public String buildUserPrompt(QuestionGenerationContext ctx) {
        return """
            Generate exactly %d distinct SYSTEM DESIGN interview prompts tailored to %s at %s.
            
            %s
            """.formatted(ctx.getQuestionCount(), ctx.safeRole(), ctx.safeCompany(), commonContextBlock(ctx));
    }

    @Override
    public List<GeneratedQuestionDraft> fallbackQuestions(QuestionGenerationContext ctx) {
        return List.of(
                baseDraft("Design a URL Shortener (like bit.ly). Cover functional/non-functional requirements, APIs, data model, hashing, caching, and scaling to billions of redirects.",
                        ctx, List.of("How do you handle custom aliases and analytics?")),
                baseDraft("Design a chat application like WhatsApp. Discuss messaging delivery, online presence, media upload, and multi-device sync.",
                        ctx, List.of("How do you guarantee message ordering in a group chat?")),
                baseDraft("Design Instagram Feed. Discuss write vs read fan-out, ranking, caching, and celebrity (hot key) problems.",
                        ctx, List.of("How would you support Stories with 24h expiry at scale?")),
                baseDraft("Design a notification system supporting push, email, and SMS with user preferences and rate limits.",
                        ctx, List.of("How do you prevent duplicate deliveries under retries?")),
                baseDraft("Design a ride-sharing system (Uber-like). Cover matching, geospatial indexing, surge pricing, and trip state machine.",
                        ctx, List.of("How do you keep ETA accurate under network partitions?")),
                baseDraft("Design YouTube video streaming. Discuss upload pipeline, transcoding, CDN, recommendations, and comments at scale.",
                        ctx, List.of("How do you handle viral videos and hot partitions?")),
                baseDraft("Design a distributed file storage system (Dropbox-like). Cover chunking, sync, conflict resolution, and deduplication.",
                        ctx, List.of("How do you ensure strong consistency for file metadata?")),
                baseDraft("Design a banking ledger / payment transfer system focusing on consistency, idempotency, and auditability.",
                        ctx, List.of("How do you prevent double-spend under retries?"))
        );
    }
}
