package com.interviai.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main application class for InterviAI Backend.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableTransactionManagement
public class InterviAIApplication {

    public static void main(String[] args) {
        SpringApplication.run(InterviAIApplication.class, args);
    }
}