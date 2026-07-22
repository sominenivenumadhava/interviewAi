package com.interviai.backend.module.jobs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResultDto {
    private String id;
    private String title;
    private String companyName;
    private String companyLogo;
    private String companyLinkedInUrl;
    private String location;
    private String workType;
    private String employmentType;
    private String experienceLevel;
    private String salary;
    private String postedAt;
    private String applicantsCount;
    private String description;
    private List<String> skills;
    private String jobUrl;
    private String applyUrl;
    private String companyWebsite;
    private String companyIndustry;
    private String companySize;
    private String scrapedAt;
    @Builder.Default
    private boolean saved = false;
}
