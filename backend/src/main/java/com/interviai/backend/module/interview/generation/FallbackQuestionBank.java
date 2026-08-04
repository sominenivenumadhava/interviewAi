package com.interviai.backend.module.interview.generation;

import com.interviai.backend.module.interview.enums.DifficultyLevel;
import com.interviai.backend.module.interview.enums.InterviewType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Large dynamic fallback banks. Always shuffle by session seed before use —
 * never return bank.get(0) as a fixed first question.
 */
public final class FallbackQuestionBank {

    private FallbackQuestionBank() {
    }

    public static List<GeneratedQuestionDraft> hr(QuestionGenerationContext ctx) {
        String role = ctx.safeRole();
        String company = ctx.safeCompany();
        List<GeneratedQuestionDraft> bank = new ArrayList<>();
        add(bank, ctx, "Walk me through a recent project that best represents your readiness for " + role + " at " + company + ".",
                "What trade-off mattered most on that project?");
        add(bank, ctx, "Why " + company + " for your next " + role + " role — what specifically attracts you beyond the brand?",
                "Which product or value of ours resonates most?");
        add(bank, ctx, "Describe a strength that peers consistently praise, and a growth area you are actively coaching yourself on.",
                "What concrete habit changed in the last quarter?");
        add(bank, ctx, "Tell me about a time you disagreed with a manager. How did you handle it?",
                "What would you do differently today?");
        add(bank, ctx, "Share a failure that taught you something durable about collaboration or delivery.",
                "How did you communicate the failure?");
        add(bank, ctx, "How do you stay motivated during ambiguous or slow-moving work?",
                "Give a recent example.");
        add(bank, ctx, "Describe a time you helped a teammate succeed when it was not your primary goal.",
                "What did you learn about influence?");
        add(bank, ctx, "What does cultural fit mean to you when joining a team like " + company + "?",
                "How do you adapt when norms differ?");
        add(bank, ctx, "Tell me about a deadline you almost missed. What did you change in your process afterward?",
                "How do you signal risk early now?");
        add(bank, ctx, "How would your last skip-level describe your working style in one sentence — and would you agree?",
                "What feedback surprised you most?");
        add(bank, ctx, "Describe a time you had to learn a skill quickly to unblock a " + role + " deliverable.",
                "How did you validate you were learning the right thing?");
        add(bank, ctx, "What kind of feedback environment helps you do your best work?",
                "How do you give hard feedback to peers?");
        add(bank, ctx, "Tell me about a goal you set that stretched you. Did you hit it? Why or why not?",
                "How do you measure progress weekly?");
        add(bank, ctx, "How do you balance asking for help versus figuring things out independently?",
                "Share a time you asked too late or too early.");
        add(bank, ctx, "What would make your first 90 days as a " + role + " at " + company + " successful?",
                "What would you intentionally not do?");
        add(bank, ctx, "Describe a conflict inside a cross-functional group and your role in resolving it.",
                "What was the lasting process change?");
        add(bank, ctx, "When have you advocated for a quieter teammate's idea?",
                "What was the outcome?");
        add(bank, ctx, "How do you handle situations where priorities change mid-sprint?",
                "How do you protect quality under churn?");
        add(bank, ctx, "Tell me about a time you received tough feedback. What changed afterward?",
                "Who held you accountable?");
        add(bank, ctx, "Why should " + company + " choose you over equally qualified " + role + " candidates?",
                "What unique proof can you show?");
        return QuestionUniquenessHelper.shuffledCopy(bank, seed(ctx));
    }

    public static List<GeneratedQuestionDraft> technical(QuestionGenerationContext ctx) {
        String role = ctx.safeRole();
        String skills = ctx.safeSkills();
        List<GeneratedQuestionDraft> bank = new ArrayList<>();
        add(bank, ctx, "Explain encapsulation vs abstraction with an example from a " + role + " codebase.",
                "Where have you seen leaky abstractions hurt a team?");
        add(bank, ctx, "Compare HashMap and TreeMap (or dict vs ordered map). When would each hurt performance?",
                "How do you choose key types safely?");
        add(bank, ctx, "Walk through how a typical HTTP request flows through a layered backend for a " + role + " service.",
                "Where would you add auth and rate limiting?");
        add(bank, ctx, "Explain ACID vs BASE. When is eventual consistency acceptable for " + companyOr(ctx) + "?",
                "How do you detect inconsistency bugs?");
        add(bank, ctx, "Describe indexing strategies you have used and a query plan you improved.",
                "What is a covering index?");
        add(bank, ctx, "How do threads, processes, and async event loops differ for I/O-bound " + role + " work?",
                "When do you prefer reactive APIs?");
        add(bank, ctx, "Explain JWT vs session cookies for a microservice used by a " + role + ".",
                "How do you rotate secrets?");
        add(bank, ctx, "What is the CAP theorem implication for a multi-region datastore?",
                "Where have you chosen AP over CP?");
        add(bank, ctx, "Describe caching layers (browser, CDN, app, DB) and a stampede you prevented.",
                "How do you choose TTLs?");
        add(bank, ctx, "Explain REST idempotency and how you design PUT vs POST safely.",
                "How do you handle retries from clients?");
        add(bank, ctx, "Compare monolith vs microservices for a team of your size. What operational costs matter?",
                "When would you merge services back?");
        add(bank, ctx, "How does garbage collection (or equivalent memory management) affect latency in production?",
                "How do you diagnose a memory leak?");
        add(bank, ctx, "Explain Docker networking basics and how containers talk in Kubernetes.",
                "What is a readiness probe for?");
        add(bank, ctx, "Describe how you would secure secrets, config, and least-privilege IAM for cloud services.",
                "How do you audit access?");
        add(bank, ctx, "Given skills [" + skills + "], pick one deep topic and explain a production incident related to it.",
                "What monitoring would catch it earlier?");
        add(bank, ctx, "Explain optimistic vs pessimistic locking with a checkout/inventory example.",
                "How do you test race conditions?");
        add(bank, ctx, "How do you design pagination (offset vs cursor) for large datasets?",
                "What breaks under concurrent writes?");
        add(bank, ctx, "Describe the difference between authentication, authorization, and audit logging.",
                "Where do RBAC models fail?");
        add(bank, ctx, "Explain how TCP differs from UDP and when each fits a product feature.",
                "What does head-of-line blocking mean for HTTP/2?");
        add(bank, ctx, "Walk through how you would test a flaky integration involving a third-party API.",
                "How do you contract-test without over-mocking?");
        return QuestionUniquenessHelper.shuffledCopy(bank, seed(ctx));
    }

    public static List<GeneratedQuestionDraft> coding(QuestionGenerationContext ctx) {
        List<GeneratedQuestionDraft> bank = new ArrayList<>();
        bank.add(coding(ctx, "Pair Sum Variants", "HashMap", DifficultyLevel.EASY,
                "Given an array nums and target T, return whether any two distinct indices sum to T. Then extend to return all unique pairs.",
                "1 <= n <= 10^5", "nums=[1,4,3,2], T=5", "true / pairs (1,4),(2,3)",
                "Can you do it in O(n) time and discuss hash collisions?"));
        bank.add(coding(ctx, "K-Group Reverse", "Linked List", DifficultyLevel.MEDIUM,
                "Reverse nodes of a singly linked list in groups of k. If the final group has fewer than k nodes, leave it as-is.",
                "1 <= n <= 5000, 1 <= k <= n", "1->2->3->4->5, k=2", "2->1->4->3->5",
                "How would you reverse every k nodes in a doubly linked list?"));
        bank.add(coding(ctx, "Anagram Groups", "Strings", DifficultyLevel.MEDIUM,
                "Group an array of strings into anagram clusters. Return any order of groups.",
                "1 <= strs.length <= 10^4", "[eat,tea,tan,ate,nat,bat]", "[[eat,tea,ate],[tan,nat],[bat]]",
                "Can you avoid sorting each string?"));
        bank.add(coding(ctx, "Sliding Window Maximum", "Sliding Window", DifficultyLevel.HARD,
                "Return the max in every contiguous window of size k.",
                "1 <= n <= 10^5", "[1,3,-1,-3,5,3,6,7], k=3", "[3,3,5,5,6,7]",
                "What if you need the median of each window?"));
        bank.add(coding(ctx, "Validate BST Edges", "BST", DifficultyLevel.MEDIUM,
                "Determine if a binary tree is a valid BST. Then count how many nodes violate the BST property if invalid.",
                "0 <= n <= 10^4", "tree=[5,1,4,null,null,3,6]", "false",
                "How do you handle duplicate values under different policies?"));
        bank.add(coding(ctx, "Course Schedule Variants", "Graph", DifficultyLevel.MEDIUM,
                "Given numCourses and prerequisites, return whether you can finish all courses. Then return one valid order if possible.",
                "1 <= numCourses <= 2000", "2, [[1,0]]", "true / [0,1]",
                "Detect all independent subgraphs."));
        bank.add(coding(ctx, "Min Stack Design", "Stack", DifficultyLevel.MEDIUM,
                "Design a stack that supports push, pop, top, and getMin in O(1) amortized time.",
                "Calls up to 3*10^4", "push 2, push 0, push 3, getMin", "0",
                "Support getMax as well without hurting getMin."));
        bank.add(coding(ctx, "Kth Largest Stream", "Heap", DifficultyLevel.MEDIUM,
                "Design a class that accepts a stream of integers and returns the kth largest element so far.",
                "1 <= k <= 10^4", "k=3, stream=[4,5,8,2]", "4 after 2 is added",
                "What changes for kth smallest?"));
        bank.add(coding(ctx, "Word Search Path", "Backtracking", DifficultyLevel.MEDIUM,
                "Given a board and a word, return true if the word exists in the grid via adjacent cells (no reuse).",
                "board <= 6x6", "board=ABCE..., word=ABCCED", "true",
                "How do you find all dictionary words present on the board?"));
        bank.add(coding(ctx, "House Robber Circular", "DP", DifficultyLevel.MEDIUM,
                "Houses form a circle; adjacent houses cannot both be robbed. Maximize loot.",
                "1 <= n <= 100", "[2,3,2]", "3",
                "Generalize to a binary tree of houses."));
        bank.add(coding(ctx, "Rotated Search II", "Binary Search", DifficultyLevel.MEDIUM,
                "Search target in a rotated sorted array that may contain duplicates. Return any valid index or -1.",
                "1 <= n <= 5000", "[2,5,6,0,0,1,2], target=0", "3 or 4",
                "Explain worst-case complexity with duplicates."));
        bank.add(coding(ctx, "Decode Ways Count", "DP", DifficultyLevel.MEDIUM,
                "A message containing letters A-Z is encoded to numbers. Given a digit string, count ways to decode.",
                "1 <= s.length <= 100", "s=226", "3",
                "Handle leading zeros carefully — walk through s=06."));
        bank.add(coding(ctx, "Next Greater Element", "Stack", DifficultyLevel.MEDIUM,
                "For each element in nums, find the next greater element to its right; use -1 if none.",
                "1 <= n <= 10^5", "[2,1,2,4,3]", "[4,2,4,-1,-1]",
                "Solve the circular array variant."));
        bank.add(coding(ctx, "Island Perimeter", "Graph", DifficultyLevel.EASY,
                "Given a grid of 0/1, compute the perimeter of the island (exactly one island).",
                "1 <= m,n <= 100", "[[0,1,0],[1,1,1],[0,1,0]]", "12",
                "Extend to count lakes (0s enclosed by 1s)."));
        bank.add(coding(ctx, "Merge Intervals Plus", "Arrays", DifficultyLevel.MEDIUM,
                "Merge overlapping intervals, then insert a new interval into the merged result.",
                "0 <= n <= 10^4", "[[1,3],[2,6],[8,10]], insert [4,9]", "[[1,6],[8,10]] after merge then insert logic",
                "Do it in one pass without sorting if input is already sorted."));
        bank.add(coding(ctx, "LRU Cache Sketch", "HashMap", DifficultyLevel.HARD,
                "Implement get/put for an LRU cache of capacity capacity with O(1) average operations.",
                "1 <= capacity <= 3000", "capacity=2; put(1,1); put(2,2); get(1); put(3,3); get(2)", "-1 for key 2",
                "How would LFU differ?"));
        return QuestionUniquenessHelper.shuffledCopy(bank, seed(ctx));
    }

    public static List<GeneratedQuestionDraft> systemDesign(QuestionGenerationContext ctx) {
        String company = ctx.safeCompany();
        List<GeneratedQuestionDraft> bank = new ArrayList<>();
        add(bank, ctx, "Design a URL shortener for " + company + ". Focus on unique hash generation, custom aliases, and analytics at scale.",
                "How do you handle hot keys and abuse?");
        add(bank, ctx, "Design a notification fan-out system (push/email/SMS) with preference management and quiet hours.",
                "How do you prevent duplicate deliveries?");
        add(bank, ctx, "Design a food-delivery matching platform: restaurants, couriers, ETA, and surge.",
                "How do you keep ETAs honest under load?");
        add(bank, ctx, "Design Instagram-like Stories with 24h expiry, views, and celebrity accounts.",
                "How do you expire media cheaply?");
        add(bank, ctx, "Design WhatsApp-like 1:1 and group messaging with multi-device sync.",
                "How do you order messages in groups?");
        add(bank, ctx, "Design an Uber-like dispatch service focusing on geospatial indexing and matching.",
                "What happens during network partitions?");
        add(bank, ctx, "Design YouTube-like video upload + streaming with CDN and adaptive bitrate.",
                "How do viral videos affect partitions?");
        add(bank, ctx, "Design Google Drive-like file sync with conflict resolution and chunking.",
                "Strong vs eventual consistency for metadata?");
        add(bank, ctx, "Design a payment gateway with idempotent charges, refunds, and ledger entries.",
                "How do you prevent double-charge on retries?");
        add(bank, ctx, "Design an inventory system for flash sales with oversell prevention.",
                "How do you shard inventory hot keys?");
        add(bank, ctx, "Design a rate limiter service used by many product APIs.",
                "Token bucket vs sliding window — trade-offs?");
        add(bank, ctx, "Design a distributed job scheduler for delayed and recurring tasks.",
                "How do you guarantee at-least-once without storms?");
        add(bank, ctx, "Design a news feed ranking pipeline for a social product at " + company + ".",
                "Online vs offline features?");
        add(bank, ctx, "Design a multiplayer collaborative document editor (OT or CRDT).",
                "How do you handle offline edits?");
        add(bank, ctx, "Design a metrics/observability pipeline ingesting millions of events/sec.",
                "How do you downsample without losing incidents?");
        add(bank, ctx, "Design a search autocomplete service with typo tolerance.",
                "How do you keep suggestions fresh?");
        return QuestionUniquenessHelper.shuffledCopy(bank, seed(ctx));
    }

    public static List<GeneratedQuestionDraft> managerial(QuestionGenerationContext ctx) {
        String role = ctx.safeRole();
        List<GeneratedQuestionDraft> bank = new ArrayList<>();
        add(bank, ctx, "Describe a prioritization call you made as a " + role + " when two P0s collided. What framework did you use?",
                "How did stakeholders react?");
        add(bank, ctx, "Tell me about owning an incident end-to-end. How did you communicate and prevent recurrence?",
                "What metric improved afterward?");
        add(bank, ctx, "Share a time you mentored someone through a performance dip.",
                "How did you set measurable goals?");
        add(bank, ctx, "Describe pushing back on a scope request that would have hurt reliability.",
                "How did you keep trust?");
        add(bank, ctx, "Tell me about a project that failed. What was your ownership story?",
                "What process changed?");
        add(bank, ctx, "How do you align engineering work with customer obsession when data is incomplete?",
                "Give a concrete example.");
        add(bank, ctx, "Describe mediating a conflict between two strong engineers on an architecture choice.",
                "What decision record did you leave?");
        add(bank, ctx, "How do you decide build vs buy for a critical dependency?",
                "What risks did you accept?");
        add(bank, ctx, "Tell me about raising the bar on code quality without slowing delivery to a halt.",
                "What guardrails stuck?");
        add(bank, ctx, "Describe influencing without authority across teams.",
                "What failed before it worked?");
        add(bank, ctx, "How do you handle a teammate who consistently misses commitments?",
                "When do you escalate?");
        add(bank, ctx, "Share an architecture decision you later reversed. What signaled the need to change?",
                "How did you migrate safely?");
        add(bank, ctx, "Describe delivering bad news to executives about a slipped launch.",
                "What options did you present?");
        add(bank, ctx, "How do you balance tech debt paydown against feature velocity for a " + role + " team?",
                "Show a quarterly plan sketch.");
        add(bank, ctx, "Tell me about a time you protected customer data or privacy under pressure to ship.",
                "What trade-off did leadership accept?");
        add(bank, ctx, "Describe creating clarity when a project had no clear owner.",
                "How did you sustain momentum?");
        return QuestionUniquenessHelper.shuffledCopy(bank, seed(ctx));
    }

    public static List<GeneratedQuestionDraft> aptitude(QuestionGenerationContext ctx) {
        long s = seed(ctx);
        int a = 80 + (int) Math.floorMod(s, 80);          // train length-ish
        int t = 4 + (int) Math.floorMod(s / 7, 8);         // seconds
        int ageDiff = 12 + (int) Math.floorMod(s / 3, 20);
        int ratioA = 2 + (int) Math.floorMod(s / 11, 4);
        int ratioB = ratioA + 1 + (int) Math.floorMod(s / 13, 3);
        int seriesStart = 2 + (int) Math.floorMod(s / 17, 5);
        int pipe1 = 10 + (int) Math.floorMod(s / 19, 10);
        int pipe2 = pipe1 + 3;
        int drain = pipe2 + 5;
        int totalExpense = 100000 + (int) Math.floorMod(s / 23, 9) * 25000;
        int marketingPct = 10 + (int) Math.floorMod(s / 29, 15);

        List<GeneratedQuestionDraft> bank = new ArrayList<>();
        add(bank, ctx, "A train " + a + " m long passes a pole in " + t + " seconds. What is its speed in km/h?",
                "Show m/s to km/h conversion.");
        add(bank, ctx, "Ages of A and B are in ratio " + ratioA + ":" + ratioB + " and B is " + ageDiff + " years older than A. Find their ages.",
                "Write the equations.");
        add(bank, ctx, "Find the next number: " + seriesStart + ", " + (seriesStart * 2 + 2) + ", " + (seriesStart * 3 + 3) + ", " + (seriesStart * 4 + 4) + ", ?",
                "State the pattern clearly.");
        add(bank, ctx, "Two pipes fill a tank in " + pipe1 + " and " + pipe2 + " hours. A drain empties it in " + drain + " hours. If all three open together, how long to fill?",
                "Write the combined rate.");
        add(bank, ctx, "A pie chart: Marketing " + marketingPct + "% of total expense $" + totalExpense + ". How much is spent on Marketing?",
                "What remains if Salaries are 40%?");
        add(bank, ctx, "You have 9 identical-looking balls; one is heavier. Minimum weighings on a balance scale in the worst case?",
                "Generalize to 27 balls.");
        add(bank, ctx, "If 15 workers finish a job in 20 days, how many workers finish it in 12 days (same rate)?",
                "State the inverse proportion.");
        add(bank, ctx, "A shopkeeper marks goods 40% above cost and offers 10% discount. What is the profit percentage?",
                "Show successive percentage math.");
        add(bank, ctx, "In a row of 40 students, A is 11th from the left and B is 15th from the right. How many sit between A and B?",
                "Consider both orderings.");
        add(bank, ctx, "Odd one out and justify: Mercury, Venus, Mars, Pluto, Jupiter.",
                "Give an alternate grouping.");
        add(bank, ctx, "A man covers 1/3 of a journey at 20 km/h, 1/3 at 30 km/h, and the rest at 60 km/h. Find average speed.",
                "Use harmonic-style reasoning carefully.");
        add(bank, ctx, "If COMPUTER → RFUVQNPC by a letter shift rule, encode MACHINE with the same rule you infer.",
                "State the transformation.");
        add(bank, ctx, "Probability: a bag has 5 red and 7 blue balls. Two drawn without replacement. P(both red)?",
                "Also compute P(different colors).");
        add(bank, ctx, "Simple interest on $P grows; if SI for 3 years at 8% is $960, find principal P.",
                "Write the SI formula.");
        add(bank, ctx, "Data interpretation: sales over 5 months are 40, 55, 50, 70, 65 (units). What is the percentage increase from month 1 to month 5?",
                "What is the average monthly sale?");
        add(bank, ctx, "Puzzle: three switches, one bulb in another room. You may enter the room once. How do you find which switch controls the bulb?",
                "Explain heat/light reasoning.");
        return QuestionUniquenessHelper.shuffledCopy(bank, seed(ctx));
    }

    private static void add(List<GeneratedQuestionDraft> bank, QuestionGenerationContext ctx, String q, String followUp) {
        bank.add(GeneratedQuestionDraft.builder()
                .questionText(q)
                .category(ctx.effectiveType().getCategoryLabel())
                .difficultyLevel(ctx.getDifficulty() != null ? ctx.getDifficulty() : DifficultyLevel.MEDIUM)
                .expectedTimeMinutes(defaultMinutes(ctx.effectiveType()))
                .evaluationCriteria(new ArrayList<>(defaultCriteria(ctx.effectiveType())))
                .followUpQuestions(new ArrayList<>(List.of(followUp)))
                .build());
    }

    private static GeneratedQuestionDraft coding(
            QuestionGenerationContext ctx,
            String title,
            String topic,
            DifficultyLevel difficulty,
            String problem,
            String constraints,
            String sampleIn,
            String sampleOut,
            String followUp
    ) {
        DifficultyLevel level = ctx.getDifficulty() != null ? ctx.getDifficulty() : difficulty;
        String text = "[" + level.name() + "] " + title + " (" + topic + ")\n\n"
                + "Problem Statement:\n" + problem + "\n\n"
                + "Constraints:\n" + constraints + "\n\n"
                + "Sample Input:\n" + sampleIn + "\n\n"
                + "Sample Output:\n" + sampleOut;
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("round", "CODING");
        metadata.put("title", title);
        metadata.put("topic", topic);
        metadata.put("format", "leetcode");
        return GeneratedQuestionDraft.builder()
                .questionText(text)
                .category(InterviewType.CODING.getCategoryLabel())
                .difficultyLevel(level)
                .expectedTimeMinutes(15)
                .evaluationCriteria(new ArrayList<>(List.of(
                        "Correctness", "Time Complexity", "Space Complexity", "Edge Cases", "Communication", "Optimization")))
                .followUpQuestions(new ArrayList<>(List.of(followUp)))
                .metadata(metadata)
                .build();
    }

    private static long seed(QuestionGenerationContext ctx) {
        if (ctx.getSessionSeed() != 0L) {
            return ctx.getSessionSeed();
        }
        return QuestionUniquenessHelper.seedFrom(ctx.safeSessionId(), ctx.safeDiversityNonce());
    }

    private static String companyOr(QuestionGenerationContext ctx) {
        return ctx.safeCompany();
    }

    private static int defaultMinutes(InterviewType type) {
        return switch (type) {
            case CODING -> 15;
            case SYSTEM_DESIGN -> 20;
            case APTITUDE -> 3;
            case MANAGERIAL -> 6;
            case HR, BEHAVIORAL -> 4;
            default -> 5;
        };
    }

    private static List<String> defaultCriteria(InterviewType type) {
        return switch (type.canonicalize()) {
            case HR, BEHAVIORAL -> List.of("Communication", "Confidence", "Personality", "Cultural Fit");
            case CODING -> List.of("Correctness", "Time Complexity", "Space Complexity", "Edge Cases", "Communication", "Optimization");
            case SYSTEM_DESIGN -> List.of("Architecture", "Scalability", "Trade-offs", "Communication");
            case MANAGERIAL -> List.of("Decision Making", "Ownership", "Leadership", "Stakeholder Management");
            case APTITUDE -> List.of("Accuracy", "Logical Reasoning", "Speed", "Clarity of Approach");
            default -> List.of("Core Concepts", "Practical Knowledge", "Problem Solving", "Confidence");
        };
    }
}
