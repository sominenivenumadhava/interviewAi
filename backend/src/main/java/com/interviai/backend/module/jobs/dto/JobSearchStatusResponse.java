package com.interviai.backend.module.jobs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSearchStatusResponse {
    private UUID searchId;
    private String status;
    private String progressMessage;
    private Integer jobsCollected;
    private Integer requestedJobs;
}
