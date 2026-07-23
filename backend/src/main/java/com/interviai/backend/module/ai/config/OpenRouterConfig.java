package com.interviai.backend.module.ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OpenRouterConfig {

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.api.base-url:https://openrouter.ai/api/v1}")
    private String baseUrl;

    @Value("${openrouter.api.model:google/gemini-2.5-flash-lite}")
    private String defaultModel;

    @Value("${openrouter.api.site-url:http://localhost:5173}")
    private String siteUrl;

    @Value("${openrouter.api.app-name:InterviAI}")
    private String appName;

    @Bean
    public WebClient openRouterWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .defaultHeader("HTTP-Referer", siteUrl)
                .defaultHeader("X-Title", appName)
                .build();
    }

    public String getDefaultModel() {
        return defaultModel;
    }
}
