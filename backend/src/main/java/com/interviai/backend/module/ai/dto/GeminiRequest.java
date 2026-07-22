package com.interviai.backend.module.ai.dto;

import java.util.List;
import java.util.Map;

public class GeminiRequest {
    
    private List<Content> contents;
    private GenerationConfig generationConfig;
    private List<SafetySettings> safetySettings;
    
    // Constructors
    public GeminiRequest() {}
    
    public GeminiRequest(String prompt) {
        this.contents = List.of(new Content("user", prompt));
        this.generationConfig = GenerationConfig.defaultConfig();
    }
    
    public GeminiRequest(String prompt, GenerationConfig config) {
        this.contents = List.of(new Content("user", prompt));
        this.generationConfig = config;
    }
    
    // Getters and Setters
    public List<Content> getContents() {
        return contents;
    }
    
    public void setContents(List<Content> contents) {
        this.contents = contents;
    }
    
    public GenerationConfig getGenerationConfig() {
        return generationConfig;
    }
    
    public void setGenerationConfig(GenerationConfig generationConfig) {
        this.generationConfig = generationConfig;
    }
    
    public List<SafetySettings> getSafetySettings() {
        return safetySettings;
    }
    
    public void setSafetySettings(List<SafetySettings> safetySettings) {
        this.safetySettings = safetySettings;
    }
    
    // Inner classes
    public static class Content {
        private String role;
        private List<Part> parts;
        
        public Content(String role, String text) {
            this.role = role;
            this.parts = List.of(new Part(text));
        }
        
        // Getters and Setters
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public List<Part> getParts() { return parts; }
        public void setParts(List<Part> parts) { this.parts = parts; }
    }
    
    public static class Part {
        private String text;
        
        public Part(String text) {
            this.text = text;
        }
        
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }
    
    public static class GenerationConfig {
        private Double temperature;
        private Integer topK;
        private Double topP;
        private Integer maxOutputTokens;
        private List<String> stopSequences;
        
        public static GenerationConfig defaultConfig() {
            GenerationConfig config = new GenerationConfig();
            config.temperature = 0.7;
            config.topK = 40;
            config.topP = 0.95;
            config.maxOutputTokens = 2048;
            return config;
        }
        
        public static GenerationConfig preciseConfig() {
            GenerationConfig config = new GenerationConfig();
            config.temperature = 0.3;
            config.topK = 20;
            config.topP = 0.8;
            config.maxOutputTokens = 2048;
            return config;
        }
        
        public static GenerationConfig creativeConfig() {
            GenerationConfig config = new GenerationConfig();
            config.temperature = 0.9;
            config.topK = 50;
            config.topP = 0.95;
            config.maxOutputTokens = 2048;
            return config;
        }
        
        // Getters and Setters
        public Double getTemperature() { return temperature; }
        public void setTemperature(Double temperature) { this.temperature = temperature; }
        public Integer getTopK() { return topK; }
        public void setTopK(Integer topK) { this.topK = topK; }
        public Double getTopP() { return topP; }
        public void setTopP(Double topP) { this.topP = topP; }
        public Integer getMaxOutputTokens() { return maxOutputTokens; }
        public void setMaxOutputTokens(Integer maxOutputTokens) { this.maxOutputTokens = maxOutputTokens; }
        public List<String> getStopSequences() { return stopSequences; }
        public void setStopSequences(List<String> stopSequences) { this.stopSequences = stopSequences; }
    }
    
    public static class SafetySettings {
        private String category;
        private String threshold;
        
        // Getters and Setters
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getThreshold() { return threshold; }
        public void setThreshold(String threshold) { this.threshold = threshold; }
    }
}