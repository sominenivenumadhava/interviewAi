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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private void validateToken() {
        if (apiToken == null || apiToken.trim().isEmpty() || "your_apify_token_here".equalsIgnoreCase(apiToken.trim()) || "your_apify_token".equalsIgnoreCase(apiToken.trim())) {
            throw new BusinessException("Apify authentication failed. Check the backend API token.");
        }
    }

    /**
     * Start the Apify Actor with search inputs.
     */
    public Map<String, String> startActorRun(String linkedinUrl, JobSearchRequest request) {
        validateToken();

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

        } catch (HttpClientErrorException e) {
            handleHttpException(e);
            throw e;
        } catch (Exception e) {
            if (e instanceof BusinessException) throw (BusinessException) e;
            log.error("Failed to start Apify Actor run", e);
            throw new BusinessException("Failed to initiate scraping: " + e.getMessage());
        }
    }

    /**
     * Check run status.
     */
    public Map<String, String> getRunStatus(String runId) {
        validateToken();

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

        } catch (HttpClientErrorException e) {
            handleHttpException(e);
            throw e;
        } catch (Exception e) {
            if (e instanceof BusinessException) throw (BusinessException) e;
            log.error("Failed to check Apify Actor run status for run ID: {}", runId, e);
            throw new BusinessException("Failed to retrieve search status: " + e.getMessage());
        }
    }

    /**
     * Retrieve and transform dataset items.
     */
    public List<JobResultDto> getDatasetItems(String datasetId) {
        validateToken();
        if (datasetId == null || datasetId.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            String responseBody = restClient.get()
                    .uri("/datasets/{datasetId}/items?token={token}", datasetId, apiToken)
                    .retrieve()
                    .body(String.class);

            return resultMapper.mapDatasetItems(responseBody);

        } catch (HttpClientErrorException e) {
            handleHttpException(e);
            throw e;
        } catch (Exception e) {
            if (e instanceof BusinessException) throw (BusinessException) e;
            log.error("Failed to retrieve Apify Dataset items for dataset ID: {}", datasetId, e);
            throw new BusinessException("Failed to fetch results: " + e.getMessage());
        }
    }

    private void handleHttpException(HttpClientErrorException e) {
        log.error("Apify API call returned error status: {} - {}", e.getStatusCode(), e.getStatusText());
        if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            throw new BusinessException("Apify authentication failed. Check the backend API token.");
        } else if (e.getStatusCode() == HttpStatus.PAYMENT_REQUIRED) {
            throw new BusinessException("The Apify account does not have enough available usage credits.");
        } else if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
            throw new BusinessException("Too many searches were started. Wait briefly and try again.");
        }
    }
}
