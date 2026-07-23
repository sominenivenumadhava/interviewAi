package com.interviai.backend.module.interview.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JsonbMappingTest {

    @Test
    void questionMetadataUsesJsonJdbcType() throws Exception {
        assertJsonMapping(InterviewQuestion.class.getDeclaredField("metadata"));
    }

    @Test
    void answerJsonFieldsUseJsonJdbcType() throws Exception {
        assertJsonMapping(InterviewAnswer.class.getDeclaredField("aiEvaluation"));
        assertJsonMapping(InterviewAnswer.class.getDeclaredField("metadata"));
    }

    private void assertJsonMapping(Field field) {
        JdbcTypeCode annotation = field.getAnnotation(JdbcTypeCode.class);
        assertNotNull(annotation, () -> field.getName() + " must declare a JSON JDBC type");
        assertEquals(SqlTypes.JSON, annotation.value());
    }
}
