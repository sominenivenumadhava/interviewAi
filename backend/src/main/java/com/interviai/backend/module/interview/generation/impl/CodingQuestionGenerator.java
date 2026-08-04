package com.interviai.backend.module.interview.generation.impl;

import com.interviai.backend.module.interview.enums.DifficultyLevel;
import com.interviai.backend.module.interview.enums.InterviewType;
import com.interviai.backend.module.interview.generation.AbstractQuestionGenerator;
import com.interviai.backend.module.interview.generation.FallbackQuestionBank;
import com.interviai.backend.module.interview.generation.GeneratedQuestionDraft;
import com.interviai.backend.module.interview.generation.QuestionGenerationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CodingQuestionGenerator extends AbstractQuestionGenerator {

    @Override
    public InterviewType supportedType() {
        return InterviewType.CODING;
    }

    @Override
    public List<String> defaultEvaluationCriteria() {
        return List.of(
                "Correctness",
                "Time Complexity",
                "Space Complexity",
                "Edge Cases",
                "Communication",
                "Optimization"
        );
    }

    @Override
    public boolean isValidCategory(String category) {
        return categoryMatches(category, "coding", "dsa", "algorithm", "leetcode", "oa");
    }

    @Override
    protected int defaultExpectedMinutes() {
        return 15;
    }

    @Override
    public String buildSystemPrompt(QuestionGenerationContext ctx) {
        return """
            You are a coding-round interviewer (LeetCode / HackerRank / company OA style) at %s for a %s role (%s).
            Session ID: %s — invent fresh problems; NEVER always start with Two Sum or Valid Parentheses.
            
            STRICT RULES — CODING ROUND ONLY:
            - Generate ONLY algorithmic coding problems. Difficulty mix should respect overall level %s.
            - Topics ONLY from: Arrays, Strings, HashMap, Sliding Window, Linked List, Stack, Queue, Tree, BST,
              Heap, Graph, Backtracking, Greedy, DP, Binary Search, Recursion — diversify topics across the set.
            - Randomize: problem statement wording, inputs, outputs, examples, variable names, constraints,
              edge cases, and follow-up questions. Even for a familiar concept, produce a different variant
              (e.g. reverse every K nodes instead of reverse entire list).
            - Follow-ups should vary (optimize, edge cases, limited memory, large N, parallelize) —
              do NOT always ask only "What is the time complexity?".
            - FORBIDDEN: "Tell me about yourself", project background, HR questions, system design case studies,
              pure theory without a problem to solve.
            - Every question category MUST be exactly "Coding".
            - Generate completely new questions different from previously generated ones.
              Avoid semantic duplication and repeated wording.
            - Each question MUST include problemStatement, constraints, sampleInput, sampleOutput,
              difficulty, expectedTimeComplexity, expectedSpaceComplexity, followUpQuestion, topic.
            
            Return raw JSON only (no markdown fences):
            {
              "questions": [
                {
                  "category": "Coding",
                  "topic": "Linked List",
                  "title": "Descriptive unique title",
                  "problemStatement": "...",
                  "constraints": "...",
                  "sampleInput": "...",
                  "sampleOutput": "...",
                  "difficulty": "MEDIUM",
                  "expectedTimeComplexity": "O(n)",
                  "expectedSpaceComplexity": "O(1)",
                  "followUpQuestion": "...",
                  "evaluationCriteria": ["Correctness", "Time Complexity", "Space Complexity", "Edge Cases", "Communication", "Optimization"],
                  "expectedTimeMinutes": 15
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
            Generate exactly %d distinct CODING problems suitable for a live coding interview.
            Preferred language context: %s. Vary topics across the allowed DSA list.
            Do not reuse problems from the avoid-list; change examples and constraints.
            
            %s
            """.formatted(
                ctx.getQuestionCount(),
                blank(ctx.getPreferredLanguage(), "any mainstream language"),
                commonContextBlock(ctx)
        );
    }

    @Override
    public GeneratedQuestionDraft parseQuestion(Map<String, Object> raw, QuestionGenerationContext context, int order) {
        String category = asString(raw, "category");
        if (category != null && !category.isBlank() && !isValidCategory(category)) {
            return null;
        }

        String problem = firstNonBlank(
                asString(raw, "problemStatement"),
                asString(raw, "question"),
                asString(raw, "questionText")
        );
        if (problem == null) {
            return null;
        }

        String title = asString(raw, "title");
        String topic = asString(raw, "topic");
        String constraints = asString(raw, "constraints");
        String sampleIn = asString(raw, "sampleInput");
        String sampleOut = asString(raw, "sampleOutput");
        String timeCx = asString(raw, "expectedTimeComplexity");
        String spaceCx = asString(raw, "expectedSpaceComplexity");
        String followUp = asString(raw, "followUpQuestion");
        List<String> followUps = asStringList(raw, "followUpQuestions");
        if (followUp != null && !followUp.isBlank()) {
            followUps = new ArrayList<>(followUps);
            followUps.add(0, followUp);
        }
        List<String> criteria = asStringList(raw, "evaluationCriteria");
        if (criteria.isEmpty()) {
            criteria = new ArrayList<>(defaultEvaluationCriteria());
        }

        DifficultyLevel difficulty = parseDifficulty(asString(raw, "difficulty"), context);
        String formatted = formatCodingProblem(title, topic, problem, constraints, sampleIn, sampleOut, difficulty, timeCx, spaceCx);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("round", "CODING");
        metadata.put("title", title);
        metadata.put("topic", topic);
        metadata.put("problemStatement", problem);
        metadata.put("constraints", constraints);
        metadata.put("sampleInput", sampleIn);
        metadata.put("sampleOutput", sampleOut);
        metadata.put("expectedTimeComplexity", timeCx);
        metadata.put("expectedSpaceComplexity", spaceCx);
        metadata.put("format", "leetcode");

        Integer minutes = null;
        Object et = raw.get("expectedTimeMinutes");
        if (et instanceof Number n) {
            minutes = n.intValue();
        }

        return GeneratedQuestionDraft.builder()
                .questionText(formatted)
                .category(InterviewType.CODING.getCategoryLabel())
                .difficultyLevel(difficulty)
                .expectedTimeMinutes(minutes != null ? minutes : defaultExpectedMinutes())
                .evaluationCriteria(criteria)
                .followUpQuestions(followUps)
                .hints(asString(raw, "hints"))
                .metadata(metadata)
                .build();
    }

    @Override
    public List<GeneratedQuestionDraft> fallbackQuestions(QuestionGenerationContext ctx) {
        return FallbackQuestionBank.coding(ctx);
    }

    private GeneratedQuestionDraft codingDraft(
            QuestionGenerationContext ctx,
            String title,
            String topic,
            DifficultyLevel difficulty,
            String problem,
            String constraints,
            String sampleIn,
            String sampleOut,
            String timeCx,
            String spaceCx,
            String followUp
    ) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("round", "CODING");
        metadata.put("title", title);
        metadata.put("topic", topic);
        metadata.put("problemStatement", problem);
        metadata.put("constraints", constraints);
        metadata.put("sampleInput", sampleIn);
        metadata.put("sampleOutput", sampleOut);
        metadata.put("expectedTimeComplexity", timeCx);
        metadata.put("expectedSpaceComplexity", spaceCx);
        metadata.put("format", "leetcode");

        DifficultyLevel level = ctx.getDifficulty() != null ? ctx.getDifficulty() : difficulty;

        return GeneratedQuestionDraft.builder()
                .questionText(formatCodingProblem(title, topic, problem, constraints, sampleIn, sampleOut, level, timeCx, spaceCx))
                .category(InterviewType.CODING.getCategoryLabel())
                .difficultyLevel(level)
                .expectedTimeMinutes(defaultExpectedMinutes())
                .evaluationCriteria(new ArrayList<>(defaultEvaluationCriteria()))
                .followUpQuestions(List.of(followUp))
                .metadata(metadata)
                .build();
    }

    private String formatCodingProblem(
            String title,
            String topic,
            String problem,
            String constraints,
            String sampleIn,
            String sampleOut,
            DifficultyLevel difficulty,
            String timeCx,
            String spaceCx
    ) {
        StringBuilder sb = new StringBuilder();
        if (title != null && !title.isBlank()) {
            sb.append("[").append(difficulty != null ? difficulty.name() : "MEDIUM").append("] ");
            sb.append(title.trim());
            if (topic != null && !topic.isBlank()) {
                sb.append(" (").append(topic.trim()).append(")");
            }
            sb.append("\n\n");
        }
        sb.append("Problem Statement:\n").append(problem.trim()).append("\n\n");
        if (constraints != null && !constraints.isBlank()) {
            sb.append("Constraints:\n").append(constraints.trim()).append("\n\n");
        }
        if (sampleIn != null && !sampleIn.isBlank()) {
            sb.append("Sample Input:\n").append(sampleIn.trim()).append("\n\n");
        }
        if (sampleOut != null && !sampleOut.isBlank()) {
            sb.append("Sample Output:\n").append(sampleOut.trim()).append("\n\n");
        }
        if (timeCx != null && !timeCx.isBlank()) {
            sb.append("Expected Time Complexity: ").append(timeCx.trim()).append("\n");
        }
        if (spaceCx != null && !spaceCx.isBlank()) {
            sb.append("Expected Space Complexity: ").append(spaceCx.trim()).append("\n");
        }
        return sb.toString().trim();
    }

    private String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v.trim();
            }
        }
        return null;
    }
}
