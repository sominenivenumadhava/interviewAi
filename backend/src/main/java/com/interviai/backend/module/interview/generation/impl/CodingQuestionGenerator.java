package com.interviai.backend.module.interview.generation.impl;

import com.interviai.backend.module.interview.enums.DifficultyLevel;
import com.interviai.backend.module.interview.enums.InterviewType;
import com.interviai.backend.module.interview.generation.AbstractQuestionGenerator;
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
            You are a coding-round interviewer (LeetCode / HackerRank / company OA style) at %s for a %s role.
            
            STRICT RULES — CODING ROUND ONLY:
            - Generate ONLY algorithmic coding problems. Difficulty mix should respect overall level %s
              (Easy / Medium / Hard problems as appropriate).
            - Topics ONLY from: Arrays, Strings, HashMap, Sliding Window, Linked List, Stack, Queue, Tree, BST,
              Heap, Graph, Backtracking, Greedy, DP, Binary Search, Recursion.
            - FORBIDDEN: "Tell me about yourself", project background, HR questions, system design case studies,
              pure theory without a problem to solve.
            - Every question category MUST be exactly "Coding".
            - Each question MUST include problemStatement, constraints, sampleInput, sampleOutput,
              difficulty, expectedTimeComplexity, expectedSpaceComplexity, followUpQuestion, topic.
            
            Return raw JSON only (no markdown fences):
            {
              "questions": [
                {
                  "category": "Coding",
                  "topic": "Arrays",
                  "title": "Two Sum",
                  "problemStatement": "Given an array of integers nums and an integer target...",
                  "constraints": "1 <= nums.length <= 10^4\\n-10^9 <= nums[i] <= 10^9",
                  "sampleInput": "nums = [2,7,11,15], target = 9",
                  "sampleOutput": "[0,1]",
                  "difficulty": "EASY",
                  "expectedTimeComplexity": "O(n)",
                  "expectedSpaceComplexity": "O(n)",
                  "followUpQuestion": "Can you solve it in one pass?",
                  "evaluationCriteria": ["Correctness", "Time Complexity", "Space Complexity", "Edge Cases", "Communication", "Optimization"],
                  "expectedTimeMinutes": 15
                }
              ]
            }
            """.formatted(ctx.safeCompany(), ctx.safeRole(), ctx.safeDifficulty());
    }

    @Override
    public String buildUserPrompt(QuestionGenerationContext ctx) {
        return """
            Generate exactly %d distinct CODING problems suitable for a live coding interview.
            Preferred language context: %s. Vary topics across the allowed DSA list.
            
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
        return List.of(
                codingDraft(ctx, "Two Sum", "Arrays", DifficultyLevel.EASY,
                        "Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target. Assume exactly one solution and you may not use the same element twice.",
                        "2 <= nums.length <= 10^4\n-10^9 <= nums[i] <= 10^9\n-10^9 <= target <= 10^9",
                        "nums = [2,7,11,15], target = 9", "[0,1]",
                        "O(n)", "O(n)", "Can you solve it in one pass with a hash map?"),
                codingDraft(ctx, "Valid Parentheses", "Stack", DifficultyLevel.EASY,
                        "Given a string s containing just the characters '(', ')', '{', '}', '[' and ']', determine if the input string is valid. Open brackets must be closed by the same type in the correct order.",
                        "1 <= s.length <= 10^4\ns consists of parentheses only",
                        "s = \"()[]{}\"", "true",
                        "O(n)", "O(n)", "How would you extend this to also validate angle brackets?"),
                codingDraft(ctx, "Longest Substring Without Repeating Characters", "Sliding Window", DifficultyLevel.MEDIUM,
                        "Given a string s, find the length of the longest substring without repeating characters.",
                        "0 <= s.length <= 5 * 10^4\ns consists of English letters, digits, symbols and spaces",
                        "s = \"abcabcbb\"", "3",
                        "O(n)", "O(min(n, alphabet))", "Solve it with a sliding window and explain the window invariants."),
                codingDraft(ctx, "Merge Two Sorted Lists", "Linked List", DifficultyLevel.EASY,
                        "You are given the heads of two sorted linked lists list1 and list2. Merge the two lists into one sorted list and return the head of the merged list.",
                        "The number of nodes in both lists is in the range [0, 50]\n-100 <= Node.val <= 100\nBoth lists are sorted in non-decreasing order",
                        "list1 = [1,2,4], list2 = [1,3,4]", "[1,1,2,3,4,4]",
                        "O(n + m)", "O(1)", "Can you do it iteratively without allocating new nodes except the dummy?"),
                codingDraft(ctx, "Binary Tree Level Order Traversal", "Tree", DifficultyLevel.MEDIUM,
                        "Given the root of a binary tree, return the level order traversal of its nodes' values (i.e., from left to right, level by level).",
                        "The number of nodes is in the range [0, 2000]\n-1000 <= Node.val <= 1000",
                        "root = [3,9,20,null,null,15,7]", "[[3],[9,20],[15,7]]",
                        "O(n)", "O(n)", "How would you produce zigzag (spiral) level order instead?"),
                codingDraft(ctx, "Number of Islands", "Graph", DifficultyLevel.MEDIUM,
                        "Given an m x n 2D binary grid which represents a map of '1' (land) and '0' (water), return the number of islands. An island is surrounded by water and formed by connecting adjacent lands horizontally or vertically.",
                        "m == grid.length\nn == grid[i].length\n1 <= m, n <= 300\ngrid[i][j] is '0' or '1'",
                        "grid = [[\"1\",\"1\",\"0\"],[\"1\",\"1\",\"0\"],[\"0\",\"0\",\"1\"]]", "2",
                        "O(m*n)", "O(m*n)", "Compare DFS vs BFS vs Union-Find for this problem."),
                codingDraft(ctx, "Climbing Stairs", "DP", DifficultyLevel.EASY,
                        "You are climbing a staircase. It takes n steps to reach the top. Each time you can climb 1 or 2 steps. In how many distinct ways can you climb to the top?",
                        "1 <= n <= 45",
                        "n = 3", "3",
                        "O(n)", "O(1)", "Generalize to up to k steps at a time."),
                codingDraft(ctx, "Search in Rotated Sorted Array", "Binary Search", DifficultyLevel.MEDIUM,
                        "There is an integer array nums sorted in ascending order (with distinct values). Prior to being passed to your function, nums is possibly rotated at an unknown pivot. Given the array nums after the possible rotation and an integer target, return the index of target if it is in nums, or -1 if it is not. You must write an algorithm with O(log n) runtime complexity.",
                        "1 <= nums.length <= 5000\n-10^4 <= nums[i] <= 10^4\nAll values of nums are unique\nnums is an ascending array that is possibly rotated\n-10^4 <= target <= 10^4",
                        "nums = [4,5,6,7,0,1,2], target = 0", "4",
                        "O(log n)", "O(1)", "How does the solution change if duplicates are allowed?")
        );
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
