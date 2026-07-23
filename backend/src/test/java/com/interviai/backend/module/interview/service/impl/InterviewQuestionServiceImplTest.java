package com.interviai.backend.module.interview.service.impl;

import com.interviai.backend.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InterviewQuestionServiceImplTest {

    private final InterviewQuestionServiceImpl service = new InterviewQuestionServiceImpl();

    @Test
    void extractsJsonFromMarkdownWrappedAiResponse() {
        String response = """
                ```json
                {"questions":[{"question":"Explain your Spring Boot caching choice."}]}
                ```
                """;

        assertEquals(
                "{\"questions\":[{\"question\":\"Explain your Spring Boot caching choice.\"}]}",
                service.extractJsonObject(response)
        );
    }

    @Test
    void rejectsAiResponseWithoutJson() {
        assertThrows(
                BusinessException.class,
                () -> service.extractJsonObject("I could not generate questions.")
        );
    }
}
