package com.interviai.backend.module.jobs.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSearchRequest {

    @NotBlank(message = "Designation/Job Title is required")
    @Size(min = 2, message = "Designation must be at least 2 characters")
    private String designation;

    @NotBlank(message = "Location is required")
    private String location;

    @Min(value = 1, message = "Number of jobs must be at least 1")
    @Max(value = 500, message = "Number of jobs cannot exceed 500")
    @Builder.Default
    private Integer numberOfJobs = 25;

    private String experienceLevel; // INTERNSHIP, ENTRY_LEVEL, ASSOCIATE, MID_SENIOR_LEVEL, DIRECTOR, EXECUTIVE

    private String workType; // ON_SITE, REMOTE, HYBRID, ANY

    private String employmentType; // FULL_TIME, PART_TIME, CONTRACT, TEMPORARY, INTERNSHIP, VOLUNTEER

    private String datePosted; // ANY_TIME, PAST_24_HOURS, PAST_WEEK, PAST_MONTH

    @Builder.Default
    private Boolean scrapeCompanyDetails = true;

    @Builder.Default
    private Boolean splitByCity = false;

    private String country;
}
