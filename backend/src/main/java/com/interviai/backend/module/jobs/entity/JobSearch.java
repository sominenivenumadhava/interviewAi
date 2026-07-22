package com.interviai.backend.module.jobs.entity;

import com.interviai.backend.common.entity.BaseEntity;
import com.interviai.backend.module.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_searches")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class JobSearch extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "designation", nullable = false)
    private String designation;

    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "filters_json", columnDefinition = "TEXT")
    private String filtersJson;

    @Column(name = "linkedin_search_url", length = 1000)
    private String linkedinSearchUrl;

    @Column(name = "apify_run_id")
    private String apifyRunId;

    @Column(name = "apify_dataset_id")
    private String apifyDatasetId;

    @Column(name = "status", nullable = false)
    private String status; // PENDING, RUNNING, SUCCEEDED, FAILED, ABORTED, TIMED_OUT

    @Column(name = "requested_job_count")
    private Integer requestedJobCount;

    @Column(name = "result_count")
    private Integer resultCount;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
