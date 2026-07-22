package com.interviai.backend.module.ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GeminiConfig {
    
    @Value("${gemini.api.key}")
    private String apiKey;
    
    @Value("${gemini.api.base-url:https://generativelanguage.googleapis.com}")
    private String baseUrl;
    
    @Value("${gemini.api.model:gemini-pro}")
    private String defaultModel;
    
    @Bean
    public WebClient geminiWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("x-goog-api-key", apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public String getDefaultModel() {
        return defaultModel;
    }
}