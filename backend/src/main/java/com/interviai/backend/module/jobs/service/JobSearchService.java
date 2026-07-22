package com.interviai.backend.module.jobs.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviai.backend.common.exception.BusinessException;
import com.interviai.backend.module.jobs.dto.*;
import com.interviai.backend.module.jobs.entity.JobSearch;
import com.interviai.backend.module.jobs.entity.SavedJob;
import com.interviai.backend.module.jobs.repository.JobSearchRepository;
import com.interviai.backend.module.jobs.repository.SavedJobRepository;
import com.interviai.backend.module.user.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JobSearchService {

    private static final Logger log = LoggerFactory.getLogger(JobSearchService.class);

    private final JobSearchRepository jobSearchRepository;
    private final SavedJobRepository savedJobRepository;
    private final LinkedInSearchUrlBuilder urlBuilder;
    private final ApifyJobScraperService scraperService;
    private final ObjectMapper objectMapper;

    public JobSearchService(
            JobSearchRepository jobSearchRepository,
            SavedJobRepository savedJobRepository,
            LinkedInSearchUrlBuilder urlBuilder,
            ApifyJobScraperService scraperService,
            ObjectMapper objectMapper) {
        this.jobSearchRepository = jobSearchRepository;
        this.savedJobRepository = savedJobRepository;
        this.urlBuilder = urlBuilder;
        this.scraperService = scraperService;
        this.objectMapper = objectMapper;
    }

    /**
     * Start a new job search.
     */
    @Transactional
    public JobSearchResponse startSearch(JobSearchRequest request, User user) {
        // Build LinkedIn search URL
        String linkedinUrl = urlBuilder.buildUrl(request);

        // Serialize filters to JSON for persistence
        String filtersJson;
        try {
            filtersJson = objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            log.error("Failed to serialize search filters", e);
            filtersJson = "{}";
        }

        // Create temporary search entry in PENDING state
        JobSearch jobSearch = JobSearch.builder()
                .user(user)
                .designation(request.getDesignation())
                .location(request.getLocation())
                .filtersJson(filtersJson)
                .linkedinSearchUrl(linkedinUrl)
                .status("PENDING")
                .requestedJobCount(request.getNumberOfJobs())
                .resultCount(0)
                .build();
        jobSearch = jobSearchRepository.save(jobSearch);

        try {
            // Start the Apify run
            Map<String, String> runDetails = scraperService.startActorRun(linkedinUrl, request);

            jobSearch.setApifyRunId(runDetails.get("runId"));
            jobSearch.setApifyDatasetId(runDetails.get("datasetId"));
            jobSearch.setStatus(runDetails.get("status"));
            jobSearch = jobSearchRepository.save(jobSearch);

            return JobSearchResponse.builder()
                    .searchId(jobSearch.getId())
                    .apifyRunId(jobSearch.getApifyRunId())
                    .status(jobSearch.getStatus())
                    .message("Job search started successfully")
                    .build();

        } catch (Exception e) {
            log.error("Failed to complete starting the Apify actor", e);
            jobSearch.setStatus("FAILED");
            jobSearch.setErrorMessage(e.getMessage());
            jobSearch.setCompletedAt(LocalDateTime.now());
            jobSearchRepository.save(jobSearch);
            throw e;
        }
    }

    /**
     * Get search status (polls Apify if still running).
     */
    @Transactional
    public JobSearchStatusResponse getSearchStatus(UUID searchId, User user) {
        JobSearch search = jobSearchRepository.findByIdAndUser(searchId, user)
                .orElseThrow(() -> new BusinessException("Job search not found"));

        String currentStatus = search.getStatus();

        // If running or pending, fetch latest status from Apify
        if ("PENDING".equals(currentStatus) || "RUNNING".equals(currentStatus) || "READY".equals(currentStatus)) {
            if (search.getApifyRunId() != null) {
                try {
                    Map<String, String> runDetails = scraperService.getRunStatus(search.getApifyRunId());
                    String newStatus = runDetails.get("status");
                    
                    search.setStatus(newStatus);
                    if (runDetails.get("datasetId") != null && !runDetails.get("datasetId").isEmpty()) {
                        search.setApifyDatasetId(runDetails.get("datasetId"));
                    }

                    if ("SUCCEEDED".equals(newStatus)) {
                        search.setCompletedAt(LocalDateTime.now());
                        // Fetch count of jobs
                        List<JobResultDto> jobs = scraperService.getDatasetItems(search.getApifyDatasetId());
                        search.setResultCount(jobs.size());
                    } else if ("FAILED".equals(newStatus) || "ABORTED".equals(newStatus) || "TIMED-OUT".equals(newStatus)) {
                        search.setCompletedAt(LocalDateTime.now());
                        search.setErrorMessage("Actor run ended with status: " + newStatus);
                    }

                    search = jobSearchRepository.save(search);
                    currentStatus = newStatus;

                } catch (Exception e) {
                    log.error("Error checking status from Apify", e);
                    // Do not fail the status check, return DB state
                }
            }
        }

        String progressMsg = mapStatusToProgressMessage(currentStatus);
        int jobsCollected = search.getResultCount() != null ? search.getResultCount() : 0;

        return JobSearchStatusResponse.builder()
                .searchId(search.getId())
                .status(currentStatus)
                .progressMessage(progressMsg)
                .jobsCollected(jobsCollected)
                .requestedJobs(search.getRequestedJobCount())
                .build();
    }

    /**
     * Get job results from the dataset.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getSearchResults(UUID searchId, User user) {
        JobSearch search = jobSearchRepository.findByIdAndUser(searchId, user)
                .orElseThrow(() -> new BusinessException("Job search not found"));

        if (!"SUCCEEDED".equals(search.getStatus())) {
            Map<String, Object> response = new HashMap<>();
            response.put("searchId", search.getId());
            response.put("status", search.getStatus());
            response.put("totalResults", 0);
            response.put("jobs", Collections.emptyList());
            return response;
        }

        List<JobResultDto> jobs = scraperService.getDatasetItems(search.getApifyDatasetId());

        // Check which jobs are saved by the user
        Set<String> savedJobIds = savedJobRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(SavedJob::getExternalJobId)
                .collect(Collectors.toSet());

        for (JobResultDto job : jobs) {
            if (savedJobIds.contains(job.getId())) {
                job.setSaved(true);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("searchId", search.getId());
        response.put("status", search.getStatus());
        response.put("totalResults", jobs.size());
        response.put("jobs", jobs);

        return response;
    }

    /**
     * Get search history.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getSearchHistory(User user) {
        return jobSearchRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(search -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", search.getId());
                    map.put("designation", search.getDesignation());
                    map.put("location", search.getLocation());
                    map.put("status", search.getStatus());
                    map.put("resultCount", search.getResultCount());
                    map.put("requestedJobCount", search.getRequestedJobCount());
                    map.put("createdAt", search.getCreatedAt());
                    map.put("completedAt", search.getCompletedAt());
                    map.put("linkedinSearchUrl", search.getLinkedinSearchUrl());
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * Save a job.
     */
    @Transactional
    public void saveJob(String jobId, JobResultDto jobDto, User user) {
        if (savedJobRepository.existsByExternalJobIdAndUser(jobId, user)) {
            return; // Already saved
        }

        String rawDataJson;
        try {
            rawDataJson = objectMapper.writeValueAsString(jobDto);
        } catch (Exception e) {
            log.error("Failed to serialize job details", e);
            rawDataJson = "{}";
        }

        SavedJob savedJob = SavedJob.builder()
                .user(user)
                .externalJobId(jobId)
                .title(jobDto.getTitle())
                .companyName(jobDto.getCompanyName())
                .location(jobDto.getLocation())
                .jobUrl(jobDto.getJobUrl())
                .rawDataJson(rawDataJson)
                .build();

        savedJobRepository.save(savedJob);
    }

    /**
     * Remove a saved job.
     */
    @Transactional
    public void removeSavedJob(String jobId, User user) {
        SavedJob savedJob = savedJobRepository.findByExternalJobIdAndUser(jobId, user)
                .orElseThrow(() -> new BusinessException("Saved job not found"));
        savedJobRepository.delete(savedJob);
    }

    /**
     * Get all saved jobs.
     */
    @Transactional(readOnly = true)
    public List<JobResultDto> getSavedJobs(User user) {
        return savedJobRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(savedJob -> {
                    try {
                        JobResultDto dto = objectMapper.readValue(savedJob.getRawDataJson(), JobResultDto.class);
                        dto.setSaved(true);
                        return dto;
                    } catch (Exception e) {
                        log.error("Failed to deserialize saved job details", e);
                        // Fallback in case rawDataJson parsing fails
                        return JobResultDto.builder()
                                .id(savedJob.getExternalJobId())
                                .title(savedJob.getTitle())
                                .companyName(savedJob.getCompanyName())
                                .location(savedJob.getLocation())
                                .jobUrl(savedJob.getJobUrl())
                                .saved(true)
                                .build();
                    }
                })
                .collect(Collectors.toList());
    }

    private String mapStatusToProgressMessage(String status) {
        if (status == null) return "Preparing job search";
        switch (status.toUpperCase()) {
            case "PENDING":
                return "Starting Apify Actor";
            case "RUNNING":
                return "Scraping LinkedIn job listings";
            case "SUCCEEDED":
                return "Search completed";
            case "FAILED":
                return "The job search could not be completed";
            case "ABORTED":
                return "The job search was aborted";
            case "TIMED-OUT":
                return "The search took longer than expected";
            default:
                return "Processing results";
        }
    }
}
