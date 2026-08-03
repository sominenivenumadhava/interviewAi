package com.interviai.backend.module.interview.generation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviai.backend.module.ai.service.AIService;
import com.interviai.backend.module.interview.enums.InterviewType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Factory that returns the dedicated generator for the selected interview round.
 */
@Component
public class QuestionGeneratorFactory {

    private static final Logger log = LoggerFactory.getLogger(QuestionGeneratorFactory.class);

    private final Map<InterviewType, QuestionGenerationStrategy> strategies = new EnumMap<>(InterviewType.class);
    private final AIService aiService;
    private final ObjectMapper objectMapper;

    public QuestionGeneratorFactory(
            AIService aiService,
            ObjectMapper objectMapper,
            List<QuestionGenerationStrategy> strategyList
    ) {
        this.aiService = aiService;
        this.objectMapper = objectMapper;
        for (QuestionGenerationStrategy strategy : strategyList) {
            strategies.put(strategy.supportedType(), strategy);
        }
        log.info("Registered question generators: {}", strategies.keySet());
    }

    public QuestionGenerationStrategy getStrategy(InterviewType type) {
        InterviewType canonical = type != null ? type.canonicalize() : InterviewType.TECHNICAL;
        QuestionGenerationStrategy strategy = strategies.get(canonical);
        if (strategy == null) {
            throw new IllegalStateException("No QuestionGenerationStrategy registered for " + canonical);
        }
        return strategy;
    }

    public AIService aiService() {
        return aiService;
    }

    public ObjectMapper objectMapper() {
        return objectMapper;
    }
}
