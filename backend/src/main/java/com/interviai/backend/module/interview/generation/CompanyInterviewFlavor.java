package com.interviai.backend.module.interview.generation;

import java.util.Locale;

/**
 * Company-specific interview flavor hints injected into generation prompts.
 * Flavor text is varied by session seed so the same company never gets an identical prompt block.
 */
public final class CompanyInterviewFlavor {

    private CompanyInterviewFlavor() {
    }

    public static String flavorFor(String company, long sessionSeed) {
        String c = company != null ? company.toLowerCase(Locale.ROOT).trim() : "";
        String base;
        if (contains(c, "google")) {
            base = "Google style: emphasize analytical depth, structured thinking, and clear problem decomposition.";
        } else if (contains(c, "amazon")) {
            base = "Amazon style: Leadership Principles, ownership, customer obsession, and pragmatic problem solving.";
        } else if (contains(c, "microsoft")) {
            base = "Microsoft style: technical depth, collaboration, growth mindset, and solid fundamentals.";
        } else if (contains(c, "phonepe")) {
            base = "PhonePe style: backend systems, Android/mobile when relevant, payments scale, and system design.";
        } else if (contains(c, "flipkart")) {
            base = "Flipkart style: scalability, marketplace systems, high throughput, and operational excellence.";
        } else if (contains(c, "adobe")) {
            base = "Adobe style: DSA strength, OOP design sense, product craftsmanship, and clean abstractions.";
        } else if (contains(c, "tcs") || contains(c, "tata consultancy")) {
            base = "TCS campus/style: fundamentals, communication clarity, aptitude-friendly framing, and process discipline.";
        } else if (contains(c, "infosys")) {
            base = "Infosys style: strong fundamentals, structured answers, and clarity on core CS / role basics.";
        } else if (contains(c, "accenture")) {
            base = "Accenture style: client communication, delivery ownership, and practical problem solving.";
        } else if (contains(c, "meta") || contains(c, "facebook")) {
            base = "Meta style: product sense, impact at scale, and rigorous technical clarity.";
        } else if (contains(c, "netflix")) {
            base = "Netflix style: high ownership, judgment calls, and systems that handle massive scale.";
        } else if (contains(c, "uber")) {
            base = "Uber style: real-time systems, geospatial thinking, reliability, and marketplace dynamics.";
        } else if (c.isBlank() || "the company".equals(c)) {
            base = "General top-tier company style: rigorous, fair, and role-aligned.";
        } else {
            base = "Match " + company + "'s hiring style: role-relevant depth, realistic scenarios, and clear evaluation signals.";
        }

        String[] twists = {
                " Vary scenarios; do not reuse classic textbook phrasing.",
                " Prefer fresh examples over famous default interview prompts.",
                " Change wording, numbers, and situational framing every time.",
                " Avoid repeating the same opener or stock problem titles.",
                " Introduce a new angle on familiar concepts rather than cloning prior prompts."
        };
        int idx = (int) Math.floorMod(sessionSeed, twists.length);
        return base + twists[idx];
    }

    private static boolean contains(String haystack, String needle) {
        return haystack.contains(needle);
    }
}
