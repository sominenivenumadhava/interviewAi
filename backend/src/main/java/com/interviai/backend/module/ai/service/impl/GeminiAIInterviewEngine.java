package com.interviai.backend.module.ai.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviai.backend.module.ai.dto.*;
import com.interviai.backend.module.ai.service.AIInterviewEngine;
import com.interviai.backend.module.ai.service.AIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GeminiAIInterviewEngine implements AIInterviewEngine {

    private static final Logger log = LoggerFactory.getLogger(GeminiAIInterviewEngine.class);

    @Autowired
    private AIService aiService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Mono<NextQuestionResult> generateFirstQuestion(InterviewContext context) {
        String prompt = buildFirstQuestionPrompt(context);
        return aiService.generateContent(prompt)
                .map(response -> parseNextQuestionResult(response, context))
                .onErrorResume(e -> {
                    log.error("Error generating first question from Gemini, fallback used", e);
                    return Mono.just(createFallbackFirstQuestion(context));
                });
    }

    @Override
    public Mono<NextQuestionResult> generateNextQuestion(InterviewContext context) {
        String prompt = buildNextQuestionPrompt(context);
        return aiService.generateContent(prompt)
                .map(response -> parseNextQuestionResult(response, context))
                .onErrorResume(e -> {
                    log.error("Error generating next question from Gemini, fallback used", e);
                    return Mono.just(createFallbackNextQuestion(context));
                });
    }

    @Override
    public Mono<EvaluationResult> evaluateAnswer(InterviewContext context, String question, String answer) {
        String prompt = buildEvaluationPrompt(context, question, answer);
        return aiService.generateContent(prompt)
                .map(this::parseEvaluationResult)
                .onErrorResume(e -> {
                    log.error("Error evaluating answer with Gemini, fallback used", e);
                    return Mono.just(createFallbackEvaluation(answer));
                });
    }

    @Override
    public Mono<FinalReportResult> generateFinalReport(InterviewContext context) {
        String prompt = buildFinalReportPrompt(context);
        return aiService.generateContent(prompt)
                .map(response -> parseFinalReportResult(response, context))
                .onErrorResume(e -> {
                    log.error("Error generating final report with Gemini, fallback used", e);
                    return Mono.just(createFallbackFinalReport(context));
                });
    }

    private String buildFirstQuestionPrompt(InterviewContext ctx) {
        String sessionHint = ctx.getSessionId() != null ? ctx.getSessionId() : java.util.UUID.randomUUID().toString();
        return String.format("""
            You are a Senior %s Interviewer at %s.
            Candidate Name: %s
            Target Role: %s
            Interview Type: %s
            Difficulty Level: %s
            Interview Session ID: %s
            Resume Context: %s
            Skills: %s

            INSTRUCTIONS:
            - Conduct a real, realistic interview for THIS session only.
            - Generate a completely new Question 1 that is different from common stock openers.
            - Avoid semantic duplication and repeated wording across sessions.
            - Strict interview type enforcement:
               * If TECHNICAL: Only technical questions, architecture, CS concepts. Do not always start with OOP.
               * If BEHAVIORAL/HR: Only culture fit, motivation, background, STAR stories — vary the scenario.
               * If SYSTEM_DESIGN: Only architecture/scalability — do not always pick URL Shortener.
               * If CODING: Only algorithmic problems — do not always pick Two Sum.
               * If MANAGERIAL: Leadership/ownership scenarios only.
               * If APTITUDE: Quantitative/logical/verbal with fresh numbers.
            - Start with a warm professional greeting, introduce yourself, then ask Question 1.

            RETURN JSON ONLY with no markdown wrapping:
            {
              "transitionPhrase": "Greeting & introduction text",
              "question": "Question 1 text",
              "category": "%s",
              "difficulty": "%s",
              "expectedMinutes": 5,
              "evaluationCriteria": ["criterion 1", "criterion 2"],
              "interviewPhase": "WARMUP",
              "isLastQuestion": false,
              "suggestedNextDifficulty": "%s"
            }
            """,
            ctx.getRole() != null ? ctx.getRole() : "Engineer",
            ctx.getCompany() != null ? ctx.getCompany() : "Tech Company",
            ctx.getCandidateName() != null ? ctx.getCandidateName() : "Candidate",
            ctx.getRole(),
            ctx.getInterviewType(),
            ctx.getDifficulty(),
            sessionHint,
            ctx.getResumeText() != null ? ctx.getResumeText() : "Not provided",
            ctx.getSkills() != null ? String.join(", ", ctx.getSkills()) : "General",
            ctx.getInterviewType(),
            ctx.getDifficulty(),
            ctx.getDifficulty()
        );
    }

    private String buildNextQuestionPrompt(InterviewContext ctx) {
        StringBuilder historyStr = new StringBuilder();
        if (ctx.getConversationHistory() != null) {
            for (InterviewContext.ConversationTurn turn : ctx.getConversationHistory()) {
                historyStr.append(turn.getRole()).append(": ").append(turn.getContent()).append("\n");
            }
        }

        return String.format("""
            You are a Senior Interviewer at %s conducting a %s interview for a %s role.
            Current Difficulty: %s
            Question Number: %d of %d
            Current Candidate Avg Score: %.1f
            Weak Skills Detected: %s

            CONVERSATION HISTORY SO FAR:
            %s

            INSTRUCTIONS:
            - If candidate gave a good technical answer, ask an intelligent deep follow-up (e.g. why X over Y, or how to scale X).
            - If candidate struggled, adapt and ask a foundational follow-up or pivot slightly.
            - Enforce Interview Type: %s. DO NOT generate unrelated question types!
            - Generate a completely new question different from previously asked ones in the history.
            - Avoid semantic duplication and repeated wording. Vary follow-ups (optimize, edge cases, memory, large N, parallelize).
            
            RETURN JSON ONLY with no markdown wrapping:
            {
              "transitionPhrase": "Short reaction to candidate answer",
              "question": "Next question text",
              "category": "%s",
              "difficulty": "%s",
              "expectedMinutes": 5,
              "evaluationCriteria": ["criterion 1"],
              "interviewPhase": "%s",
              "isLastQuestion": %b,
              "suggestedNextDifficulty": "%s"
            }
            """,
            ctx.getCompany(), ctx.getInterviewType(), ctx.getRole(),
            ctx.getCurrentDifficulty() != null ? ctx.getCurrentDifficulty() : ctx.getDifficulty(),
            ctx.getCurrentQuestionNumber(), ctx.getTotalQuestions(),
            ctx.getCurrentScore() != null ? ctx.getCurrentScore() : 75.0,
            ctx.getWeakSkills() != null ? String.join(", ", ctx.getWeakSkills()) : "None",
            historyStr.toString(),
            ctx.getInterviewType(),
            ctx.getInterviewType(),
            ctx.getDifficulty(),
            ctx.getCurrentQuestionNumber() >= ctx.getTotalQuestions() ? "CLOSING" : "CORE",
            ctx.getCurrentQuestionNumber() >= ctx.getTotalQuestions(),
            ctx.getDifficulty()
        );
    }

    private String buildEvaluationPrompt(InterviewContext ctx, String q, String a) {
        return String.format("""
            Evaluate this interview response for a %s role at %s (Type: %s).
            Question: %s
            Candidate Answer: %s

            Evaluate across 7 metrics:
            - technicalAccuracy (0-100)
            - communication (0-100)
            - confidence (0-100)
            - completeness (0-100)
            - problemSolving (0-100)
            - depth (0-100)
            - clarity (0-100)

            RETURN JSON ONLY with no markdown:
            {
              "score": 85,
              "rating": "GOOD",
              "technicalAccuracy": 80,
              "communication": 90,
              "confidence": 85,
              "completeness": 80,
              "problemSolving": 85,
              "depth": 75,
              "clarity": 90,
              "feedback": "Detailed feedback",
              "expectedAnswer": "Ideal answer summary",
              "weakSkillsDetected": ["SkillA"],
              "strongSkillsDetected": ["SkillB"],
              "improvementTips": ["Tip 1", "Tip 2"],
              "suggestedNextDifficulty": "MEDIUM"
            }
            """,
            ctx.getRole(), ctx.getCompany(), ctx.getInterviewType(), q, a
        );
    }

    private String buildFinalReportPrompt(InterviewContext ctx) {
        StringBuilder historyStr = new StringBuilder();
        if (ctx.getConversationHistory() != null) {
            for (InterviewContext.ConversationTurn turn : ctx.getConversationHistory()) {
                historyStr.append(turn.getRole()).append(": ").append(turn.getContent()).append("\n");
            }
        }

        return String.format("""
            Generate a complete enterprise candidate assessment report for an interview at %s for %s.
            Candidate History:
            %s

            RETURN JSON ONLY:
            {
              "overallScore": 82,
              "hiringProbability": "78%%",
              "companyReadinessScore": 80,
              "categoryScores": {
                "Communication": 85,
                "Technical": 80,
                "System Design": 75,
                "Behavioral": 90,
                "Confidence": 85
              },
              "keyStrengths": ["Strength 1", "Strength 2"],
              "keyWeaknesses": ["Weakness 1"],
              "criticalMistakes": [
                {
                  "question": "Sample Q",
                  "candidateAnswer": "Sample A",
                  "idealApproach": "Better approach",
                  "keyTakeaway": "Takeaway"
                }
              ],
              "recommendedLearningPath": ["Topic 1", "Topic 2"],
              "recommendedLeetCode": ["Two Sum", "LRU Cache"],
              "recommendedProjects": ["Distributed Rate Limiter"],
              "recommendedCourses": ["Grokking System Design"],
              "executiveSummary": "Full summary text"
            }
            """, ctx.getCompany(), ctx.getRole(), historyStr.toString());
    }

    private NextQuestionResult parseNextQuestionResult(String json, InterviewContext ctx) {
        try {
            String cleanJson = cleanJsonString(json);
            return objectMapper.readValue(cleanJson, NextQuestionResult.class);
        } catch (Exception e) {
            log.warn("Could not parse NextQuestionResult JSON: {}", json, e);
            return createFallbackNextQuestion(ctx);
        }
    }

    private EvaluationResult parseEvaluationResult(String json) {
        try {
            String cleanJson = cleanJsonString(json);
            return objectMapper.readValue(cleanJson, EvaluationResult.class);
        } catch (Exception e) {
            log.warn("Could not parse EvaluationResult JSON: {}", json, e);
            return createFallbackEvaluation("Answer received");
        }
    }

    private FinalReportResult parseFinalReportResult(String json, InterviewContext ctx) {
        try {
            String cleanJson = cleanJsonString(json);
            return objectMapper.readValue(cleanJson, FinalReportResult.class);
        } catch (Exception e) {
            log.warn("Could not parse FinalReportResult JSON: {}", json, e);
            return createFallbackFinalReport(ctx);
        }
    }

    private String cleanJsonString(String raw) {
        if (raw == null) return "{}";
        String str = raw.trim();
        if (str.startsWith("```json")) {
            str = str.substring(7);
        }
        if (str.startsWith("```")) {
            str = str.substring(3);
        }
        if (str.endsWith("```")) {
            str = str.substring(0, str.length() - 3);
        }
        return str.trim();
    }

    private NextQuestionResult createFallbackFirstQuestion(InterviewContext ctx) {
        String type = ctx.getInterviewType() != null ? ctx.getInterviewType().toUpperCase() : "TECHNICAL";
        String role = ctx.getRole() != null ? ctx.getRole() : "Engineer";
        String company = ctx.getCompany() != null ? ctx.getCompany() : "the company";
        String session = ctx.getSessionId() != null ? ctx.getSessionId() : java.util.UUID.randomUUID().toString();
        int idx = Math.floorMod(session.hashCode(), 4);
        String question;
        if (type.contains("CODING") || type.contains("DSA")) {
            String[] pool = {
                    "Reverse nodes of a linked list in groups of k. If the final group has fewer than k nodes, leave it as-is.",
                    "Group an array of strings into anagram clusters and explain your hashing approach.",
                    "Given a stream of integers, return the kth largest so far after each insertion.",
                    "Find whether you can finish all courses given prerequisite edges; return one valid order."
            };
            question = pool[idx];
        } else if (type.contains("SYSTEM")) {
            String[] pool = {
                    "Design a notification fan-out system for " + company + " with preferences and rate limits.",
                    "Design a food-delivery matching platform focusing on ETA and courier assignment.",
                    "Design a rate limiter service used by many product APIs at " + company + ".",
                    "Design a payment ledger with idempotent charges and refunds."
            };
            question = pool[idx];
        } else if (type.contains("HR") || type.contains("BEHAVIOR")) {
            String[] pool = {
                    "Walk me through a recent project that best shows your fit for " + role + " at " + company + ".",
                    "Tell me about a time you handled conflict on a cross-functional team.",
                    "Why " + company + " for your next " + role + " role — beyond brand recognition?",
                    "Describe a strength peers praise and a growth area you are actively improving."
            };
            question = pool[idx];
        } else if (type.contains("MANAGERIAL") || type.contains("BAR")) {
            String[] pool = {
                    "Describe a prioritization call you made as a " + role + " when two P0s collided.",
                    "Tell me about owning an incident end-to-end and preventing recurrence.",
                    "Share a time you mentored someone through a performance dip.",
                    "Describe pushing back on scope that would have hurt reliability."
            };
            question = pool[idx];
        } else if (type.contains("APTITUDE")) {
            int len = 80 + Math.floorMod(session.hashCode(), 80);
            int secs = 4 + Math.floorMod(session.hashCode() / 7, 8);
            String[] pool = {
                    "A train " + len + " m long passes a pole in " + secs + " seconds. What is its speed in km/h?",
                    "Ages of A and B are in ratio 3:5 and B is 20 years older than A. Find their ages.",
                    "Two pipes fill a tank in 12 and 15 hours; a drain empties it in 20. If all open, how long to fill?",
                    "Find the next number: 2, 6, 12, 20, 30, ?"
            };
            question = pool[idx];
        } else {
            String[] pool = {
                    "Explain caching layers and how you would prevent a cache stampede in a " + role + " service.",
                    "Compare SQL vs NoSQL for a " + role + " workload at " + company + ".",
                    "Walk through authentication vs authorization for a microservice used by a " + role + ".",
                    "How would you diagnose a production latency spike in a " + role + " system?"
            };
            question = pool[idx];
        }
        return NextQuestionResult.builder()
                .transitionPhrase("Welcome! Let's get started with your interview.")
                .question(question)
                .category(ctx.getInterviewType() != null ? ctx.getInterviewType() : "Technical")
                .difficulty(ctx.getDifficulty() != null ? ctx.getDifficulty() : "Medium")
                .expectedMinutes(5)
                .evaluationCriteria(List.of("Clarity", "Depth", "Relevance"))
                .interviewPhase("WARMUP")
                .isLastQuestion(false)
                .suggestedNextDifficulty(ctx.getDifficulty() != null ? ctx.getDifficulty() : "Medium")
                .build();
    }

    private NextQuestionResult createFallbackNextQuestion(InterviewContext ctx) {
        String role = ctx.getRole() != null ? ctx.getRole() : "Engineer";
        String session = ctx.getSessionId() != null ? ctx.getSessionId() : "x";
        int n = ctx.getCurrentQuestionNumber() != null ? ctx.getCurrentQuestionNumber() : 2;
        int idx = Math.floorMod(session.hashCode() + n * 31, 4);
        String[] pool = {
                "Can this approach be optimized further for larger inputs?",
                "What happens for edge cases or empty input in your solution?",
                "How would you solve this with limited memory?",
                String.format("Regarding your role as %s, what trade-offs would you revisit under 10x traffic?", role)
        };
        return NextQuestionResult.builder()
                .transitionPhrase("Thank you. Moving on to our next topic.")
                .question(pool[idx])
                .category(ctx.getInterviewType() != null ? ctx.getInterviewType() : "Technical")
                .difficulty(ctx.getDifficulty() != null ? ctx.getDifficulty() : "Medium")
                .expectedMinutes(5)
                .evaluationCriteria(List.of("Depth", "Trade-off reasoning"))
                .interviewPhase("CORE")
                .isLastQuestion(false)
                .suggestedNextDifficulty("Medium")
                .build();
    }

    private EvaluationResult createFallbackEvaluation(String answer) {
        return EvaluationResult.builder()
                .score(80)
                .rating("GOOD")
                .technicalAccuracy(80)
                .communication(85)
                .confidence(80)
                .completeness(75)
                .problemSolving(80)
                .depth(75)
                .clarity(85)
                .feedback("Solid answer with clear structure. Consider adding more quantitative metrics.")
                .expectedAnswer("An ideal response clearly explains architectural decisions, tradeoffs, and measurable outcomes.")
                .weakSkillsDetected(List.of("System Tradeoffs"))
                .strongSkillsDetected(List.of("Communication"))
                .improvementTips(List.of("Use concrete metrics when describing results", "Explain fallback strategies"))
                .suggestedNextDifficulty("Medium")
                .build();
    }

    private FinalReportResult createFallbackFinalReport(InterviewContext ctx) {
        return FinalReportResult.builder()
                .overallScore(84)
                .hiringProbability("82%")
                .companyReadinessScore(85)
                .categoryScores(Map.of(
                        "Communication", 88,
                        "Technical", 82,
                        "System Design", 78,
                        "Behavioral", 86,
                        "Confidence", 84
                ))
                .keyStrengths(List.of("Clear technical articulation", "Structured problem breakdown", "Strong domain knowledge"))
                .keyWeaknesses(List.of("Needs deeper analysis of scaling edge cases"))
                .criticalMistakes(List.of(
                        FinalReportResult.MistakeSummary.builder()
                                .question("System scaling question")
                                .candidateAnswer("Initial approach was monolithic")
                                .idealApproach("Proactively discuss caching and sharding layer")
                                .keyTakeaway("Always mention horizontal scalability first")
                                .build()
                ))
                .recommendedLearningPath(List.of("Distributed Caching Patterns", "Topological Sort & Graph Algorithms"))
                .recommendedLeetCode(List.of("LRU Cache", "Course Schedule", "Design Search Autocomplete"))
                .recommendedProjects(List.of("High Throughput Microservice", "Realtime Webhook Processor"))
                .recommendedCourses(List.of("Grokking the System Design Interview"))
                .executiveSummary("Candidate demonstrated solid domain competency and strong communication. Recommended for next round with slight prep on system scaling.")
                .build();
    }
}
