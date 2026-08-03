package com.interviai.backend.module.interview.generation;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Helpers for session seeding, shuffling, and near-duplicate question detection.
 */
public final class QuestionUniquenessHelper {

    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9\\s]+");
    private static final Pattern MULTI_SPACE = Pattern.compile("\\s+");

    private QuestionUniquenessHelper() {
    }

    public static long seedFrom(String sessionId, String nonce) {
        String material = (sessionId != null ? sessionId : "") + "|" + (nonce != null ? nonce : "")
                + "|" + System.currentTimeMillis();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(material.getBytes(StandardCharsets.UTF_8));
            long seed = 0L;
            for (int i = 0; i < 8; i++) {
                seed = (seed << 8) | (dig[i] & 0xffL);
            }
            return seed == 0L ? 1L : seed;
        } catch (Exception e) {
            return (sessionId != null ? sessionId.hashCode() : 42L) * 31L
                    + (nonce != null ? nonce.hashCode() : 7L)
                    + System.nanoTime();
        }
    }

    public static <T> List<T> shuffledCopy(List<T> source, long seed) {
        List<T> copy = new ArrayList<>(source);
        Collections.shuffle(copy, new Random(seed));
        return copy;
    }

    public static String normalize(String text) {
        if (text == null) {
            return "";
        }
        String n = text.toLowerCase(Locale.ROOT);
        n = NON_ALNUM.matcher(n).replaceAll(" ");
        n = MULTI_SPACE.matcher(n).replaceAll(" ").trim();
        return n;
    }

    /** Jaccard similarity over word tokens; 1.0 = identical bag of words. */
    public static double similarity(String a, String b) {
        Set<String> wa = tokens(normalize(a));
        Set<String> wb = tokens(normalize(b));
        if (wa.isEmpty() && wb.isEmpty()) {
            return 1.0;
        }
        if (wa.isEmpty() || wb.isEmpty()) {
            return 0.0;
        }
        Set<String> inter = new LinkedHashSet<>(wa);
        inter.retainAll(wb);
        Set<String> union = new LinkedHashSet<>(wa);
        union.addAll(wb);
        return union.isEmpty() ? 0.0 : (double) inter.size() / (double) union.size();
    }

    public static boolean isTooSimilar(String candidate, List<String> existing, double threshold) {
        if (candidate == null || candidate.isBlank()) {
            return true;
        }
        String norm = normalize(candidate);
        for (String prev : existing) {
            if (prev == null || prev.isBlank()) {
                continue;
            }
            String pn = normalize(prev);
            if (norm.equals(pn) || norm.contains(pn) || pn.contains(norm)) {
                return true;
            }
            if (similarity(norm, pn) >= threshold) {
                return true;
            }
        }
        return false;
    }

    public static String extractTopicHint(String questionText) {
        String n = normalize(questionText);
        if (n.isBlank()) {
            return "general";
        }
        // Prefer first meaningful 4–6 tokens as a soft topic fingerprint
        String[] parts = n.split(" ");
        StringBuilder sb = new StringBuilder();
        int added = 0;
        for (String p : parts) {
            if (p.length() < 3 || isStop(p)) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(p);
            added++;
            if (added >= 5) {
                break;
            }
        }
        return sb.length() > 0 ? sb.toString() : n.substring(0, Math.min(40, n.length()));
    }

    private static Set<String> tokens(String normalized) {
        Set<String> set = new LinkedHashSet<>();
        for (String t : normalized.split(" ")) {
            if (t.length() >= 2 && !isStop(t)) {
                set.add(t);
            }
        }
        return set;
    }

    private static boolean isStop(String w) {
        return Set.of(
                "the", "and", "for", "with", "that", "this", "from", "your", "you", "are", "was",
                "how", "what", "when", "where", "which", "would", "could", "should", "about",
                "into", "have", "has", "had", "will", "can", "does", "did", "a", "an", "of", "to",
                "in", "on", "or", "is", "be", "as", "at", "by", "it"
        ).contains(w);
    }
}
