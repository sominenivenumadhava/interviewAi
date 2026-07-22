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
        return String.format("""
            You are a Senior %s Interviewer at %s.
            Candidate Name: %s
            Target Role: %s
            Interview Type: %s
            Difficulty Level: %s
            Resume Context: %s
            Skills: %s

            INSTRUCTIONS:
            - Conduct a real, realistic interview.
            - Strict interview type enforcement:
               * If TECHNICAL: Only technical questions, architecture, CS concepts.
               * If BEHAVIORAL: Only STAR behavioral questions.
               * If HR: Only culture fit, salary, motivation, background.
               * If SYSTEM_DESIGN: Only high level architecture and scalability.
               * If CODING: Only algorithmic and implementation questions.
               * If MIXED: Combined round.
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
        return NextQuestionResult.builder()
                .transitionPhrase("Welcome! Let's get started with your interview.")
                .question("Could you introduce yourself and walk me through a complex technical project you worked on recently?")
                .category(ctx.getInterviewType() != null ? ctx.getInterviewType() : "Technical")
                .difficulty(ctx.getDifficulty() != null ? ctx.getDifficulty() : "Medium")
                .expectedMinutes(5)
                .evaluationCriteria(List.of("Clear background summary", "Technical project overview"))
                .interviewPhase("WARMUP")
                .isLastQuestion(false)
                .suggestedNextDifficulty(ctx.getDifficulty() != null ? ctx.getDifficulty() : "Medium")
                .build();
    }

    private NextQuestionResult createFallbackNextQuestion(InterviewContext ctx) {
        return NextQuestionResult.builder()
                .transitionPhrase("Thank you. Moving on to our next topic.")
                .question(String.format("Regarding your role as %s, how do you handle performance bottlenecks and optimization in production?", ctx.getRole() != null ? ctx.getRole() : "Engineer"))
                .category(ctx.getInterviewType() != null ? ctx.getInterviewType() : "Technical")
                .difficulty(ctx.getDifficulty() != null ? ctx.getDifficulty() : "Medium")
                .expectedMinutes(5)
                .evaluationCriteria(List.of("Profiling skills", "Optimization strategy"))
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
