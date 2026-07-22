package com.interviai.backend.module.jobs.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviai.backend.module.jobs.dto.JobResultDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class ApifyResultMapper {

    private static final Logger log = LoggerFactory.getLogger(ApifyResultMapper.class);
    private final ObjectMapper objectMapper;

    public ApifyResultMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<JobResultDto> mapDatasetItems(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            List<JobResultDto> results = new ArrayList<>();
            if (rootNode.isArray()) {
                for (JsonNode itemNode : rootNode) {
                    try {
                        results.add(mapSingleItem(itemNode));
                    } catch (Exception e) {
                        log.error("Failed to map single job item node", e);
                    }
                }
            } else {
                results.add(mapSingleItem(rootNode));
            }
            return results;
        } catch (Exception e) {
            log.error("Failed to parse dataset JSON", e);
            return Collections.emptyList();
        }
    }

    public JobResultDto mapSingleItem(JsonNode node) {
        if (node == null || node.isNull()) {
            return new JobResultDto();
        }

        String id = getAsString(node, "id", "jobId", "postId");
        if (id == null || id.isEmpty()) {
            id = UUID.randomUUID().toString();
        }

        String title = getAsString(node, "title");
        String companyName = getAsString(node, "companyName", "company");
        String companyLogo = getAsString(node, "companyLogo", "logo");
        String companyLinkedInUrl = getAsString(node, "companyLinkedinUrl", "companyUrl");
        String location = getAsString(node, "location");
        String workType = getAsString(node, "workType");
        String employmentType = getAsString(node, "contractType", "employmentType");
        String experienceLevel = getAsString(node, "experienceLevel");
        
        String salary = null;
        JsonNode salaryNode = node.get("salary");
        if (salaryNode == null) {
            salaryNode = node.get("salaryInfo");
        }
        if (salaryNode != null && !salaryNode.isNull()) {
            if (salaryNode.isArray()) {
                List<String> salaryList = new ArrayList<>();
                for (JsonNode s : salaryNode) {
                    salaryList.add(s.asText());
                }
                salary = String.join(" - ", salaryList);
            } else {
                salary = salaryNode.asText();
            }
        }

        String postedAt = getAsString(node, "postedAt", "postedTime", "publishedAt");
        String applicantsCount = getAsString(node, "applicantsCount", "applicationsCount");
        String description = getAsString(node, "descriptionHtml", "description");
        if (description == null || description.isEmpty()) {
            description = getAsString(node, "descriptionText");
        }

        List<String> skills = new ArrayList<>();
        JsonNode skillsNode = node.get("skills");
        if (skillsNode != null && !skillsNode.isNull()) {
            if (skillsNode.isArray()) {
                for (JsonNode s : skillsNode) {
                    skills.add(s.asText());
                }
            } else {
                String skillsText = skillsNode.asText();
                if (skillsText != null && !skillsText.isEmpty()) {
                    skills = Arrays.asList(skillsText.split(",\\s*"));
                }
            }
        }

        String jobUrl = getAsString(node, "jobUrl", "link");
        String applyUrl = getAsString(node, "applyUrl");
        if (applyUrl == null || applyUrl.isEmpty()) {
            applyUrl = jobUrl;
        }

        String companyWebsite = getAsString(node, "companyWebsite", "website");
        String companyIndustry = getAsString(node, "companyIndustry", "industry", "sector");
        String companySize = getAsString(node, "companySize", "employeesCount");
        String scrapedAt = getAsString(node, "scrapedAt");
        if (scrapedAt == null || scrapedAt.isEmpty()) {
            scrapedAt = LocalDateTime.now().toString();
        }

        return JobResultDto.builder()
                .id(id)
                .title(fallback(title))
                .companyName(fallback(companyName))
                .companyLogo(fallback(companyLogo, ""))
                .companyLinkedInUrl(fallback(companyLinkedInUrl, ""))
                .location(fallback(location))
                .workType(fallback(workType))
                .employmentType(fallback(employmentType))
                .experienceLevel(fallback(experienceLevel))
                .salary(fallback(salary, "Not provided"))
                .postedAt(fallback(postedAt))
                .applicantsCount(fallback(applicantsCount, "Not provided"))
                .description(fallback(description))
                .skills(skills)
                .jobUrl(fallback(jobUrl, ""))
                .applyUrl(fallback(applyUrl, ""))
                .companyWebsite(fallback(companyWebsite, ""))
                .companyIndustry(fallback(companyIndustry))
                .companySize(fallback(companySize))
                .scrapedAt(scrapedAt)
                .saved(false)
                .build();
    }

    private String getAsString(JsonNode node, String... keys) {
        for (String key : keys) {
            JsonNode val = node.get(key);
            if (val != null && !val.isNull()) {
                return val.asText();
            }
        }
        return null;
    }

    private String fallback(String value) {
        return fallback(value, "Not provided");
    }

    private String fallback(String value, String defaultVal) {
        return (value == null || value.trim().isEmpty()) ? defaultVal : value;
    }
}
