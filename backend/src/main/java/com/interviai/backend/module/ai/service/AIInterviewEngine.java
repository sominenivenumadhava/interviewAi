package com.interviai.backend.module.ai.service;

import com.interviai.backend.module.ai.dto.InterviewContext;
import com.interviai.backend.module.ai.dto.NextQuestionResult;
import com.interviai.backend.module.ai.dto.EvaluationResult;
import com.interviai.backend.module.ai.dto.FinalReportResult;
import reactor.core.publisher.Mono;

/**
 * Core AI Interview Engine.
 * Orchestrates the full interview session using Gemini AI:
 * - Context-rich prompt construction
 * - Type-enforced question generation (TECHNICAL|BEHAVIORAL|HR|SYSTEM_DESIGN|CODING|MIXED)
 * - Adaptive difficulty adjustment
 * - Real-time per-answer evaluation
 * - Final enterprise-level report generation
 */
public interface AIInterviewEngine {

    /**
     * Generate the opening greeting + first question for the interview.
     * The AI acts as a senior interviewer from the target company.
     */
    Mono<NextQuestionResult> generateFirstQuestion(InterviewContext context);

    /**
     * Generate the next question based on previous answer and conversation history.
     * Includes intelligent follow-up questions and adaptive difficulty.
     */
    Mono<NextQuestionResult> generateNextQuestion(InterviewContext context);

    /**
     * Evaluate a single answer in real time.
     * Returns score, feedback, expected answer, and improvement tips.
     */
    Mono<EvaluationResult> evaluateAnswer(InterviewContext context, String question, String answer);

    /**
     * Generate the complete final interview report.
     * Includes all category scores, learning path, LeetCode recommendations,
     * company readiness score, and estimated hiring probability.
     */
    Mono<FinalReportResult> generateFinalReport(InterviewContext context);
}
