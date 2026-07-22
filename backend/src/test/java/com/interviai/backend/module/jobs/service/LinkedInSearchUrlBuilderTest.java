package com.interviai.backend.module.jobs.service;

import com.interviai.backend.module.jobs.dto.JobSearchRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LinkedInSearchUrlBuilderTest {

    private LinkedInSearchUrlBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new LinkedInSearchUrlBuilder();
    }

    @Test
    void testBuildUrl_Basic() {
        JobSearchRequest request = JobSearchRequest.builder()
                .designation("Java Developer")
                .location("Bengaluru, Karnataka, India")
                .numberOfJobs(25)
                .build();

        String url = builder.buildUrl(request);

        assertNotNull(url);
        assertTrue(url.contains("keywords=Java%20Developer"));
        assertTrue(url.contains("location=Bengaluru%2C%20Karnataka%2C%20India"));
        assertTrue(url.contains("position=1"));
        assertTrue(url.contains("pageNum=0"));
    }

    @Test
    void testBuildUrl_Filters() {
        JobSearchRequest request = JobSearchRequest.builder()
                .designation("Software Engineer")
                .location("US")
                .datePosted("PAST_WEEK")
                .employmentType("FULL_TIME")
                .experienceLevel("MID_SENIOR_LEVEL")
                .build();

        String url = builder.buildUrl(request);

        assertNotNull(url);
        assertTrue(url.contains("&f_TPR=r604800")); // past week
        assertTrue(url.contains("&f_JT=F")); // full time
        assertTrue(url.contains("&f_E=4")); // mid-senior
    }

    @Test
    void testBuildUrl_DatePostedMappings() {
        // Past 24 hours
        JobSearchRequest request24 = JobSearchRequest.builder()
                .designation("Dev")
                .location("US")
                .datePosted("PAST_24_HOURS")
                .build();
        assertTrue(builder.buildUrl(request24).contains("&f_TPR=r86400"));

        // Past Month
        JobSearchRequest requestMonth = JobSearchRequest.builder()
                .designation("Dev")
                .location("US")
                .datePosted("PAST_MONTH")
                .build();
        assertTrue(builder.buildUrl(requestMonth).contains("&f_TPR=r2592000"));
    }

    @Test
    void testBuildUrl_ExperienceMappings() {
        JobSearchRequest request = JobSearchRequest.builder()
                .designation("Dev")
                .location("US")
                .experienceLevel("ENTRY_LEVEL")
                .build();
        assertTrue(builder.buildUrl(request).contains("&f_E=2"));

        JobSearchRequest requestIntern = JobSearchRequest.builder()
                .designation("Dev")
                .location("US")
                .experienceLevel("INTERNSHIP")
                .build();
        assertTrue(builder.buildUrl(requestIntern).contains("&f_E=1"));
    }
}
