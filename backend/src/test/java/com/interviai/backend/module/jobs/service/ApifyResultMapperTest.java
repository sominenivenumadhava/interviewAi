package com.interviai.backend.module.jobs.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviai.backend.module.jobs.dto.JobResultDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApifyResultMapperTest {

    private ApifyResultMapper mapper;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mapper = new ApifyResultMapper(objectMapper);
    }

    @Test
    void testMapDatasetItems_NullOrEmpty() {
        List<JobResultDto> results = mapper.mapDatasetItems(null);
        assertNotNull(results);
        assertTrue(results.isEmpty());

        results = mapper.mapDatasetItems("");
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testMapDatasetItems_ValidJsonArray() {
        String json = "[" +
                "  {" +
                "    \"id\": \"12345\"," +
                "    \"title\": \"Java Engineer\"," +
                "    \"companyName\": \"Google\"," +
                "    \"location\": \"Mountain View, CA\"," +
                "    \"contractType\": \"Full-time\"," +
                "    \"jobUrl\": \"https://linkedin.com/jobs/view/12345\"" +
                "  }" +
                "]";

        List<JobResultDto> results = mapper.mapDatasetItems(json);
        assertNotNull(results);
        assertEquals(1, results.size());

        JobResultDto job = results.get(0);
        assertEquals("12345", job.getId());
        assertEquals("Java Engineer", job.getTitle());
        assertEquals("Google", job.getCompanyName());
        assertEquals("Mountain View, CA", job.getLocation());
        assertEquals("Full-time", job.getEmploymentType());
        assertEquals("https://linkedin.com/jobs/view/12345", job.getJobUrl());
        assertEquals("https://linkedin.com/jobs/view/12345", job.getApplyUrl()); // fallback
        assertEquals("Not provided", job.getSalary());
    }

    @Test
    void testMapDatasetItems_NullSafetyFallback() {
        String json = "[{}]"; // empty object

        List<JobResultDto> results = mapper.mapDatasetItems(json);
        assertNotNull(results);
        assertEquals(1, results.size());

        JobResultDto job = results.get(0);
        assertNotNull(job.getId()); // auto-generated UUID fallback
        assertEquals("Not provided", job.getTitle());
        assertEquals("Not provided", job.getCompanyName());
        assertEquals("Not provided", job.getLocation());
        assertEquals("Not provided", job.getEmploymentType());
        assertEquals("Not provided", job.getSalary());
    }
}
