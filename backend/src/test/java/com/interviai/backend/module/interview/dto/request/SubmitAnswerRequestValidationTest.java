package com.interviai.backend.module.interview.dto.request;

import com.interviai.backend.common.constant.ApiConstants;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SubmitAnswerRequestValidationTest {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsDetailedAnswerLongerThanPreviousTenThousandCharacterLimit() {
        SubmitAnswerRequest request = requestWithAnswer("a".repeat(14_845));

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void rejectsAnswerAboveConfiguredLimit() {
        SubmitAnswerRequest request =
                requestWithAnswer("a".repeat(ApiConstants.MAX_ANSWER_LENGTH + 1));

        Set<ConstraintViolation<SubmitAnswerRequest>> violations = validator.validate(request);

        assertTrue(violations.stream().anyMatch(violation ->
                violation.getPropertyPath().toString().equals("answerText")));
    }

    private SubmitAnswerRequest requestWithAnswer(String answerText) {
        SubmitAnswerRequest request = new SubmitAnswerRequest();
        request.setSessionId("test-session");
        request.setQuestionOrder(1);
        request.setAnswerText(answerText);
        return request;
    }
}
