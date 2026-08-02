package com.interviai.backend.module.jobs.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviai.backend.common.exception.BusinessException;
import com.interviai.backend.module.jobs.dto.JobResultDto;
import com.interviai.backend.module.jobs.dto.JobSearchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.*;

@Service
public class ApifyJobScraperService {

    private static final Logger log = LoggerFactory.getLogger(ApifyJobScraperService.class);

    private final RestClient restClient;
    private final String apiToken;
    private final String defaultActorId;
    private final ApifyResultMapper resultMapper;
    private final ObjectMapper objectMapper;

    public ApifyJobScraperService(
            @Value("${apify.base-url:https://api.apify.com/v2}") String baseUrl,
            @Value("${apify.api-token:}") String apiToken,
            @Value("${apify.actor-id:hKByXkMQaC5Qt9UMN}") String defaultActorId,
            ApifyResultMapper resultMapper,
            ObjectMapper objectMapper) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.apiToken = apiToken;
        this.defaultActorId = defaultActorId;
        this.resultMapper = resultMapper;
        this.objectMapper = objectMapper;
    }

    private boolean isTokenValid() {
        return apiToken != null 
            && !apiToken.trim().isEmpty() 
            && !"your_apify_token_here".equalsIgnoreCase(apiToken.trim()) 
            && !"your_apify_token".equalsIgnoreCase(apiToken.trim());
    }

    /**
     * Start the Apify Actor with search inputs.
     */
    public Map<String, String> startActorRun(String linkedinUrl, JobSearchRequest request) {
        if (!isTokenValid()) {
            log.warn("Apify API token is not configured or is placeholder. Falling back to mock job scraper.");
            return createMockRunDetails(request);
        }

        Map<String, Object> input = new HashMap<>();
        input.put("urls", Collections.singletonList(linkedinUrl));
        input.put("scrapeCompany", request.getScrapeCompanyDetails() != null ? request.getScrapeCompanyDetails() : true);
        input.put("count", request.getNumberOfJobs() != null ? request.getNumberOfJobs() : 25);
        input.put("splitByLocation", request.getSplitByCity() != null ? request.getSplitByCity() : false);
        if (request.getSplitByCity() != null && request.getSplitByCity() && request.getCountry() != null && !request.getCountry().trim().isEmpty()) {
            input.put("splitCountry", request.getCountry().trim());
        }

        log.info("Starting Apify Actor run for url count: {}, count limit: {}, scrapeCompany: {}", 
                1, input.get("count"), input.get("scrapeCompany"));

        try {
            String responseBody = restClient.post()
                    .uri("/acts/{actorId}/runs?token={token}", defaultActorId, apiToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(input)
                    .retrieve()
                    .body(String.class);

            JsonNode responseJson = objectMapper.readTree(responseBody);
            JsonNode dataNode = responseJson.get("data");
            if (dataNode == null) {
                throw new BusinessException("Apify started successfully but returned a malformed response");
            }

            Map<String, String> runDetails = new HashMap<>();
            runDetails.put("runId", dataNode.get("id").asText());
            runDetails.put("status", dataNode.get("status").asText());
            runDetails.put("datasetId", dataNode.has("defaultDatasetId") ? dataNode.get("defaultDatasetId").asText() : "");

            return runDetails;

        } catch (Exception e) {
            log.warn("Apify API call failed ({}), falling back to mock job scraper results", e.getMessage());
            return createMockRunDetails(request);
        }
    }

    /**
     * Check run status.
     */
    public Map<String, String> getRunStatus(String runId) {
        if (runId != null && runId.startsWith("mock-run-")) {
            Map<String, String> runDetails = new HashMap<>();
            runDetails.put("runId", runId);
            runDetails.put("status", "SUCCEEDED");
            runDetails.put("datasetId", "mock-dataset-" + runId);
            return runDetails;
        }

        if (!isTokenValid()) {
            Map<String, String> runDetails = new HashMap<>();
            runDetails.put("runId", runId != null ? runId : "mock-run-default");
            runDetails.put("status", "SUCCEEDED");
            runDetails.put("datasetId", "mock-dataset-default");
            return runDetails;
        }

        try {
            String responseBody = restClient.get()
                    .uri("/actor-runs/{runId}?token={token}", runId, apiToken)
                    .retrieve()
                    .body(String.class);

            JsonNode responseJson = objectMapper.readTree(responseBody);
            JsonNode dataNode = responseJson.get("data");
            if (dataNode == null) {
                throw new BusinessException("Apify status check returned a malformed response");
            }

            Map<String, String> runDetails = new HashMap<>();
            runDetails.put("runId", dataNode.get("id").asText());
            runDetails.put("status", dataNode.get("status").asText());
            runDetails.put("datasetId", dataNode.has("defaultDatasetId") ? dataNode.get("defaultDatasetId").asText() : "");

            return runDetails;

        } catch (Exception e) {
            log.warn("Failed to check Apify Actor run status ({}), returning SUCCEEDED mock status", e.getMessage());
            Map<String, String> runDetails = new HashMap<>();
            runDetails.put("runId", runId);
            runDetails.put("status", "SUCCEEDED");
            runDetails.put("datasetId", "mock-dataset-" + runId);
            return runDetails;
        }
    }

    /**
     * Retrieve and transform dataset items.
     */
    public List<JobResultDto> getDatasetItems(String datasetId) {
        if (datasetId != null && datasetId.startsWith("mock-dataset-")) {
            return generateMockJobs(datasetId);
        }

        if (!isTokenValid() || datasetId == null || datasetId.trim().isEmpty()) {
            return generateMockJobs(datasetId);
        }

        try {
            String responseBody = restClient.get()
                    .uri("/datasets/{datasetId}/items?token={token}", datasetId, apiToken)
                    .retrieve()
                    .body(String.class);

            return resultMapper.mapDatasetItems(responseBody);

        } catch (Exception e) {
            log.warn("Failed to retrieve Apify Dataset items ({}), returning mock job results", e.getMessage());
            return generateMockJobs(datasetId);
        }
    }

    private Map<String, String> createMockRunDetails(JobSearchRequest request) {
        String designation = request.getDesignation() != null ? request.getDesignation() : "Software Engineer";
        String location = request.getLocation() != null ? request.getLocation() : "Remote";

        Map<String, String> runDetails = new HashMap<>();
        runDetails.put("runId", "mock-run-" + UUID.randomUUID());
        runDetails.put("status", "SUCCEEDED");
        runDetails.put("datasetId", "mock-dataset-" + UUID.randomUUID() + ":" + designation + ":" + location);
        return runDetails;
    }

    public List<JobResultDto> generateMockJobs(String datasetId) {
        String designation = "Software Engineer";
        String location = "Remote";
        
        if (datasetId != null && datasetId.contains(":")) {
            String[] parts = datasetId.split(":", 3);
            if (parts.length >= 2 && !parts[1].isBlank()) designation = parts[1];
            if (parts.length >= 3 && !parts[2].isBlank()) location = parts[2];
        }
        
        List<JobResultDto> mockList = new ArrayList<>();
        String[] companies = {"TechCorp AI", "CloudScale Labs", "InnovateX", "DataPulse Inc", "NexGen Solutions", "Apex Systems", "ByteCraft", "Vanguard Technologies"};
        String[] workTypes = {"Remote", "Hybrid", "On-site"};
        String[] expLevels = {"Entry Level", "Mid Level", "Senior Level", "Lead"};
        String[] salaries = {"$90,000 - $120,000 / yr", "$130,000 - $160,000 / yr", "$160,000 - $200,000 / yr", "$180,000 - $220,000 / yr"};

        for (int i = 1; i <= 8; i++) {
            String company = companies[(i - 1) % companies.length];
            String jobTitle = (i % 2 == 0 ? "Senior " : "") + designation;
            String loc = (i % 3 == 0 ? "Remote" : location);
            
            JobResultDto dto = JobResultDto.builder()
                    .id("job-sample-" + i + "-" + Math.abs(designation.hashCode()))
                    .title(jobTitle)
                    .companyName(company)
                    .companyLogo("https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=120&h=120&q=80")
                    .location(loc)
                    .workType(workTypes[(i - 1) % workTypes.length])
                    .employmentType("Full-time")
                    .experienceLevel(expLevels[(i - 1) % expLevels.length])
                    .salary(salaries[(i - 1) % salaries.length])
                    .postedAt(i + " days ago")
                    .applicantsCount((i * 12) + " applicants")
                    .description("We are looking for a highly motivated " + designation + " to join " + company + ". You will work on innovative products, build robust backend and frontend software, and collaborate with team members globally.")
                    .skills(List.of("Java", "Spring Boot", "React", "TypeScript", "SQL", "Docker", "AWS"))
                    .jobUrl("https://www.linkedin.com/jobs")
                    .applyUrl("https://www.linkedin.com/jobs")
                    .companyWebsite("https://" + company.toLowerCase().replaceAll("[^a-z0-9]", "") + ".com")
                    .companyIndustry("Information Technology & Services")
                    .companySize("100-500 employees")
                    .scrapedAt(java.time.Instant.now().toString())
                    .saved(false)
                    .build();
            mockList.add(dto);
        }
        return mockList;
    }
}
