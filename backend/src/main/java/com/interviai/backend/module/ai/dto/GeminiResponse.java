package com.interviai.backend.module.ai.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiResponse {
    
    private List<Candidate> candidates;
    private PromptFeedback promptFeedback;
    
    // Getters and Setters
    public List<Candidate> getCandidates() {
        return candidates;
    }
    
    public void setCandidates(List<Candidate> candidates) {
        this.candidates = candidates;
    }
    
    public PromptFeedback getPromptFeedback() {
        return promptFeedback;
    }
    
    public void setPromptFeedback(PromptFeedback promptFeedback) {
        this.promptFeedback = promptFeedback;
    }
    
    public String getFirstText() {
        if (candidates != null && !candidates.isEmpty()) {
            Candidate firstCandidate = candidates.get(0);
            if (firstCandidate.getContent() != null 
                && firstCandidate.getContent().getParts() != null
                && !firstCandidate.getContent().getParts().isEmpty()) {
                return firstCandidate.getContent().getParts().get(0).getText();
            }
        }
        return null;
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Candidate {
        private Content content;
        private String finishReason;
        private Integer index;
        private List<SafetyRating> safetyRatings;
        
        // Getters and Setters
        public Content getContent() { return content; }
        public void setContent(Content content) { this.content = content; }
        public String getFinishReason() { return finishReason; }
        public void setFinishReason(String finishReason) { this.finishReason = finishReason; }
        public Integer getIndex() { return index; }
        public void setIndex(Integer index) { this.index = index; }
        public List<SafetyRating> getSafetyRatings() { return safetyRatings; }
        public void setSafetyRatings(List<SafetyRating> safetyRatings) { this.safetyRatings = safetyRatings; }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Content {
        private List<Part> parts;
        private String role;
        
        // Getters and Setters
        public List<Part> getParts() { return parts; }
        public void setParts(List<Part> parts) { this.parts = parts; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Part {
        private String text;
        
        // Getters and Setters
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SafetyRating {
        private String category;
        private String probability;
        
        // Getters and Setters
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getProbability() { return probability; }
        public void setProbability(String probability) { this.probability = probability; }
    }
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PromptFeedback {
        private List<SafetyRating> safetyRatings;
        
        // Getters and Setters
        public List<SafetyRating> getSafetyRatings() { return safetyRatings; }
        public void setSafetyRatings(List<SafetyRating> safetyRatings) { this.safetyRatings = safetyRatings; }
    }
}