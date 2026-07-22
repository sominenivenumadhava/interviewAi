package com.interviai.backend.module.jobs.service;

import com.interviai.backend.module.jobs.dto.JobSearchRequest;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class LinkedInSearchUrlBuilder {

    public String buildUrl(JobSearchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("JobSearchRequest cannot be null");
        }

        StringBuilder url = new StringBuilder("https://www.linkedin.com/jobs/search/?");

        String encodedKeywords = encode(request.getDesignation());
        String encodedLocation = encode(request.getLocation());

        url.append("keywords=").append(encodedKeywords)
           .append("&location=").append(encodedLocation)
           .append("&position=1&pageNum=0");

        // Date Posted mappings
        if (request.getDatePosted() != null) {
            switch (request.getDatePosted().toUpperCase()) {
                case "PAST_24_HOURS":
                    url.append("&f_TPR=r86400");
                    break;
                case "PAST_WEEK":
                    url.append("&f_TPR=r604800");
                    break;
                case "PAST_MONTH":
                    url.append("&f_TPR=r2592000");
                    break;
                default:
                    // ANY_TIME or unsupported -> no filter
                    break;
            }
        }

        // Employment Type mappings
        if (request.getEmploymentType() != null) {
            switch (request.getEmploymentType().toUpperCase()) {
                case "FULL_TIME":
                    url.append("&f_JT=F");
                    break;
                case "PART_TIME":
                    url.append("&f_JT=P");
                    break;
                case "CONTRACT":
                    url.append("&f_JT=C");
                    break;
                case "TEMPORARY":
                    url.append("&f_JT=T");
                    break;
                case "INTERNSHIP":
                    url.append("&f_JT=I");
                    break;
                case "VOLUNTEER":
                    url.append("&f_JT=V");
                    break;
                default:
                    break;
            }
        }

        // Experience Level mappings
        if (request.getExperienceLevel() != null) {
            switch (request.getExperienceLevel().toUpperCase()) {
                case "INTERNSHIP":
                    url.append("&f_E=1");
                    break;
                case "ENTRY_LEVEL":
                    url.append("&f_E=2");
                    break;
                case "ASSOCIATE":
                    url.append("&f_E=3");
                    break;
                case "MID_SENIOR_LEVEL":
                    url.append("&f_E=4");
                    break;
                case "DIRECTOR":
                    url.append("&f_E=5");
                    break;
                case "EXECUTIVE":
                    url.append("&f_E=6");
                    break;
                default:
                    break;
            }
        }

        return url.toString();
    }

    private String encode(String value) {
        if (value == null) {
            return "";
        }
        // URLEncoder encodes spaces as '+' but encodeURIComponent encodes them as '%20'
        return URLEncoder.encode(value, StandardCharsets.UTF_8)
                         .replaceAll("\\+", "%20")
                         .replaceAll("\\%21", "!")
                         .replaceAll("\\%27", "'")
                         .replaceAll("\\%28", "(")
                         .replaceAll("\\%29", ")")
                         .replaceAll("\\%7E", "~");
    }
}
