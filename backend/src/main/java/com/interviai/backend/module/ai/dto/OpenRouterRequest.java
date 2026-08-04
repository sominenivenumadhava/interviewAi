package com.interviai.backend.module.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenRouterRequest {

    private String model;
    private List<Message> messages;
    private Double temperature;

    @JsonProperty("top_p")
    private Double topP;

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    private List<String> stop;

    public OpenRouterRequest() {
    }

    public OpenRouterRequest(String model, String prompt, GenerationConfig config) {
        this.model = model;
        this.messages = List.of(new Message("user", prompt));
        this.temperature = config.getTemperature();
        this.topP = config.getTopP();
        this.maxTokens = config.getMaxOutputTokens();
        this.stop = config.getStopSequences();
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getTopP() {
        return topP;
    }

    public void setTopP(Double topP) {
        this.topP = topP;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public List<String> getStop() {
        return stop;
    }

    public void setStop(List<String> stop) {
        this.stop = stop;
    }

    public static class Message {
        private String role;
        private String content;

        public Message() {
        }

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    public static class GenerationConfig {
        private Double temperature;
        private Double topP;
        private Integer maxOutputTokens;
        private List<String> stopSequences;

        public static GenerationConfig defaultConfig() {
            GenerationConfig config = new GenerationConfig();
            config.temperature = 0.7;
            config.topP = 0.95;
            config.maxOutputTokens = 2048;
            return config;
        }

        public static GenerationConfig preciseConfig() {
            GenerationConfig config = new GenerationConfig();
            config.temperature = 0.3;
            config.topP = 0.8;
            config.maxOutputTokens = 2048;
            return config;
        }

        /** High-variance config for interview question generation (avoid repeated questions). */
        public static GenerationConfig creativeConfig() {
            GenerationConfig config = new GenerationConfig();
            config.temperature = 0.95;
            config.topP = 0.95;
            config.maxOutputTokens = 4096;
            return config;
        }

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }

        public Double getTopP() {
            return topP;
        }

        public void setTopP(Double topP) {
            this.topP = topP;
        }

        public Integer getMaxOutputTokens() {
            return maxOutputTokens;
        }

        public void setMaxOutputTokens(Integer maxOutputTokens) {
            this.maxOutputTokens = maxOutputTokens;
        }

        public List<String> getStopSequences() {
            return stopSequences;
        }

        public void setStopSequences(List<String> stopSequences) {
            this.stopSequences = stopSequences;
        }
    }
}
