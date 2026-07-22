package com.interviai.backend.module.resume.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviai.backend.module.resume.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility class for parsing resume text and extracting structured information.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ResumeParser {

    private final ObjectMapper objectMapper;

    // Regex patterns for extraction
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "(?:\\+?1[-\\s]?)?\\(?[0-9]{3}\\)?[-\\s]?[0-9]{3}[-\\s]?[0-9]{4}|" +
            "(?:\\+?91[-\\s]?)?[0-9]{10}|" +
            "(?:\\+?[0-9]{1,3}[-\\s]?)?\\(?[0-9]{3,4}\\)?[-\\s]?[0-9]{3,4}[-\\s]?[0-9]{3,4}");
    
    private static final Pattern NAME_PATTERN = Pattern.compile(
            "^([A-Z][a-zA-Z]*(?:\\s[A-Z][a-zA-Z]*)+)");

    private static final Pattern EXPERIENCE_PATTERN = Pattern.compile(
            "(\\d+)(?:[+\\s]*)?(?:years?|yrs?)(?:[\\s]*(?:and|&|,)?[\\s]*(\\d+)(?:[\\s]*months?|mos?))?",
            Pattern.CASE_INSENSITIVE);

    // Common section headers
    private static final Set<String> EXPERIENCE_HEADERS = Set.of(
            "experience", "work experience", "professional experience", "employment history",
            "work history", "career", "professional background", "employment"
    );

    private static final Set<String> EDUCATION_HEADERS = Set.of(
            "education", "educational background", "academic background", "qualifications",
            "academic qualifications", "studies", "learning"
    );

    private static final Set<String> SKILLS_HEADERS = Set.of(
            "skills", "technical skills", "core competencies", "expertise", "technologies",
            "technical expertise", "competencies", "abilities", "proficiencies"
    );

    private static final Set<String> PROJECTS_HEADERS = Set.of(
            "projects", "project experience", "key projects", "notable projects",
            "personal projects", "academic projects", "professional projects"
    );

    /**
     * Parse resume text and extract structured information.
     */
    public ParsedResumeData parseResumeText(String text) {
        if (text == null || text.trim().isEmpty()) {
            log.warn("Resume text is null or empty");
            return ParsedResumeData.builder().build();
        }

        log.debug("Starting resume parsing for {} characters of text", text.length());

        try {
            ParsedResumeData parsedData = ParsedResumeData.builder()
                    .personalInfo(extractPersonalInfo(text))
                    .summary(extractSummary(text))
                    .workExperiences(extractWorkExperiences(text))
                    .educations(extractEducations(text))
                    .skills(extractSkills(text))
                    .projects(extractProjects(text))
                    .confidenceScore(calculateConfidenceScore(text))
                    .build();

            log.debug("Resume parsing completed with confidence score: {}", parsedData.getConfidenceScore());
            return parsedData;

        } catch (Exception e) {
            log.error("Error parsing resume text", e);
            return ParsedResumeData.builder()
                    .confidenceScore(0.0)
                    .build();
        }
    }

    /**
     * Extract personal information from resume text.
     */
    private PersonalInfo extractPersonalInfo(String text) {
        PersonalInfo.PersonalInfoBuilder builder = PersonalInfo.builder();

        // Extract email
        Matcher emailMatcher = EMAIL_PATTERN.matcher(text);
        if (emailMatcher.find()) {
            builder.email(emailMatcher.group().toLowerCase());
        }

        // Extract phone
        Matcher phoneMatcher = PHONE_PATTERN.matcher(text);
        if (phoneMatcher.find()) {
            String phone = phoneMatcher.group().replaceAll("[^0-9+]", "");
            if (phone.length() >= 10) {
                builder.phone(formatPhoneNumber(phone));
            }
        }

        // Extract name (usually in first few lines)
        String[] lines = text.split("\n");
        String name = extractName(lines);
        if (name != null) {
            builder.fullName(name);
        }

        // Extract location (look for city, state patterns)
        String location = extractLocation(text);
        if (location != null) {
            builder.location(location);
        }

        return builder.build();
    }

    /**
     * Extract summary/objective from resume text.
     */
    private String extractSummary(String text) {
        String[] summaryHeaders = {
                "summary", "professional summary", "objective", "career objective",
                "profile", "about", "about me", "overview", "career overview"
        };

        for (String header : summaryHeaders) {
            String summary = extractSectionContent(text, header);
            if (summary != null && summary.length() > 50) {
                return summary.length() > 1000 ? summary.substring(0, 1000) + "..." : summary;
            }
        }

        return null;
    }

    /**
     * Extract work experiences from resume text.
     */
    private List<WorkExperienceData> extractWorkExperiences(String text) {
        List<WorkExperienceData> experiences = new ArrayList<>();

        String experienceSection = null;
        for (String header : EXPERIENCE_HEADERS) {
            experienceSection = extractSectionContent(text, header);
            if (experienceSection != null) {
                break;
            }
        }

        if (experienceSection == null) {
            return experiences;
        }

        // Split by common patterns that separate jobs
        String[] jobEntries = experienceSection.split("(?=\\n[A-Z][^\\n]{10,}\\n|\\n\\d{4}|\\n[A-Z][a-z]+ \\d{4})");

        for (String jobEntry : jobEntries) {
            if (jobEntry.trim().length() < 20) continue;

            WorkExperienceData experience = parseWorkExperience(jobEntry.trim());
            if (experience != null) {
                experiences.add(experience);
            }
        }

        return experiences.stream().limit(10).collect(Collectors.toList()); // Limit to 10 experiences
    }

    /**
     * Extract education information from resume text.
     */
    private List<EducationData> extractEducations(String text) {
        List<EducationData> educations = new ArrayList<>();

        String educationSection = null;
        for (String header : EDUCATION_HEADERS) {
            educationSection = extractSectionContent(text, header);
            if (educationSection != null) {
                break;
            }
        }

        if (educationSection == null) {
            return educations;
        }

        // Split by patterns that separate education entries
        String[] eduEntries = educationSection.split("(?=\\n[A-Z][^\\n]{5,}\\n|\\n\\d{4}|\\n[A-Z][a-z]+\\s+of\\s+)");

        for (String eduEntry : eduEntries) {
            if (eduEntry.trim().length() < 10) continue;

            EducationData education = parseEducation(eduEntry.trim());
            if (education != null) {
                educations.add(education);
            }
        }

        return educations.stream().limit(5).collect(Collectors.toList()); // Limit to 5 educations
    }

    /**
     * Extract skills from resume text.
     */
    private List<SkillData> extractSkills(String text) {
        Set<String> skillsFound = new LinkedHashSet<>();

        String skillsSection = null;
        for (String header : SKILLS_HEADERS) {
            skillsSection = extractSectionContent(text, header);
            if (skillsSection != null) {
                break;
            }
        }

        if (skillsSection != null) {
            // Extract from dedicated skills section
            skillsFound.addAll(parseSkillsFromSection(skillsSection));
        }

        // Also extract from entire document
        skillsFound.addAll(extractCommonSkills(text));

        return skillsFound.stream()
                .limit(50) // Limit to 50 skills
                .map(this::createSkillData)
                .collect(Collectors.toList());
    }

    /**
     * Extract projects from resume text.
     */
    private List<ProjectData> extractProjects(String text) {
        List<ProjectData> projects = new ArrayList<>();

        String projectsSection = null;
        for (String header : PROJECTS_HEADERS) {
            projectsSection = extractSectionContent(text, header);
            if (projectsSection != null) {
                break;
            }
        }

        if (projectsSection == null) {
            return projects;
        }

        // Split by patterns that separate projects
        String[] projectEntries = projectsSection.split("(?=\\n[A-Z][^\\n]{5,}\\n|\\n•|\\n-)");

        for (String projectEntry : projectEntries) {
            if (projectEntry.trim().length() < 20) continue;

            ProjectData project = parseProject(projectEntry.trim());
            if (project != null) {
                projects.add(project);
            }
        }

        return projects.stream().limit(10).collect(Collectors.toList()); // Limit to 10 projects
    }

    /**
     * Extract section content by header.
     */
    private String extractSectionContent(String text, String header) {
        String pattern = "(?i)^\\s*" + Pattern.quote(header) + "\\s*:?\\s*$";
        String[] lines = text.split("\n");

        for (int i = 0; i < lines.length; i++) {
            if (lines[i].matches(pattern)) {
                // Found header, extract content until next section
                StringBuilder content = new StringBuilder();
                for (int j = i + 1; j < lines.length; j++) {
                    String line = lines[j].trim();
                    if (isNewSection(line)) {
                        break;
                    }
                    content.append(line).append("\n");
                }
                
                String result = content.toString().trim();
                return result.isEmpty() ? null : result;
            }
        }

        return null;
    }

    /**
     * Check if line is a new section header.
     */
    private boolean isNewSection(String line) {
        if (line.length() < 3 || line.length() > 50) {
            return false;
        }

        // Check against known headers
        String normalized = line.toLowerCase().replaceAll("[^a-z\\s]", "").trim();
        return EXPERIENCE_HEADERS.contains(normalized) ||
               EDUCATION_HEADERS.contains(normalized) ||
               SKILLS_HEADERS.contains(normalized) ||
               PROJECTS_HEADERS.contains(normalized) ||
               normalized.matches("contact|references|certifications?|achievements?|awards?|languages?");
    }

    /**
     * Calculate confidence score based on extracted data quality.
     */
    private Double calculateConfidenceScore(String text) {
        double score = 0.0;

        // Base score for having text
        if (text != null && text.length() > 100) {
            score += 10.0;
        }

        // Email found
        if (EMAIL_PATTERN.matcher(text).find()) {
            score += 15.0;
        }

        // Phone found
        if (PHONE_PATTERN.matcher(text).find()) {
            score += 10.0;
        }

        // Experience section found
        for (String header : EXPERIENCE_HEADERS) {
            if (text.toLowerCase().contains(header)) {
                score += 20.0;
                break;
            }
        }

        // Education section found
        for (String header : EDUCATION_HEADERS) {
            if (text.toLowerCase().contains(header)) {
                score += 15.0;
                break;
            }
        }

        // Skills section found
        for (String header : SKILLS_HEADERS) {
            if (text.toLowerCase().contains(header)) {
                score += 10.0;
                break;
            }
        }

        // Date patterns found
        if (text.matches(".*\\b(19|20)\\d{2}\\b.*")) {
            score += 10.0;
        }

        // Company names or job titles likely present
        if (text.matches(".*\\b(Software|Engineer|Developer|Manager|Analyst|Consultant)\\b.*")) {
            score += 10.0;
        }

        return Math.min(score, 100.0);
    }

    // Helper methods for parsing specific entities
    private String extractName(String[] lines) {
        for (int i = 0; i < Math.min(5, lines.length); i++) {
            String line = lines[i].trim();
            if (line.length() > 5 && line.length() < 50) {
                Matcher nameMatcher = NAME_PATTERN.matcher(line);
                if (nameMatcher.find()) {
                    return nameMatcher.group(1);
                }
            }
        }
        return null;
    }

    private String extractLocation(String text) {
        // Simple location extraction - look for city, state patterns
        Pattern locationPattern = Pattern.compile(
                "\\b([A-Z][a-z]+),\\s*([A-Z]{2})\\b|" +
                "\\b([A-Z][a-z]+),\\s*([A-Z][a-z]+)\\b"
        );
        
        Matcher matcher = locationPattern.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        
        return null;
    }

    private String formatPhoneNumber(String phone) {
        // Simple phone formatting
        if (phone.length() == 10) {
            return String.format("(%s) %s-%s", 
                    phone.substring(0, 3), 
                    phone.substring(3, 6), 
                    phone.substring(6));
        }
        return phone;
    }

    private WorkExperienceData parseWorkExperience(String jobEntry) {
        // This is a simplified parser - in a real implementation,
        // you might use more sophisticated NLP techniques
        String[] lines = jobEntry.split("\n");
        
        if (lines.length < 2) return null;

        WorkExperienceData.WorkExperienceDataBuilder builder = WorkExperienceData.builder();
        
        // Try to extract job title and company from first line
        String firstLine = lines[0].trim();
        if (firstLine.contains(" at ")) {
            String[] parts = firstLine.split(" at ");
            builder.jobTitle(parts[0].trim());
            builder.companyName(parts[1].trim());
        } else if (firstLine.contains(" - ")) {
            String[] parts = firstLine.split(" - ");
            builder.jobTitle(parts[0].trim());
            if (parts.length > 1) builder.companyName(parts[1].trim());
        }

        // Extract dates
        String dateText = extractDateRange(jobEntry);
        if (dateText != null) {
            parseWorkExperienceDates(builder, dateText);
        }

        // Extract description
        StringBuilder description = new StringBuilder();
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!line.matches(".*\\d{4}.*") && line.length() > 10) {
                description.append(line).append(" ");
            }
        }
        
        if (description.length() > 0) {
            builder.description(description.toString().trim());
        }

        return builder.build();
    }

    private EducationData parseEducation(String eduEntry) {
        String[] lines = eduEntry.split("\n");
        if (lines.length < 1) return null;

        EducationData.EducationDataBuilder builder = EducationData.builder();
        
        String firstLine = lines[0].trim();
        
        // Try to extract degree and institution
        if (firstLine.contains(" from ")) {
            String[] parts = firstLine.split(" from ");
            builder.degree(parts[0].trim());
            builder.institutionName(parts[1].trim());
        } else if (firstLine.contains(" - ")) {
            String[] parts = firstLine.split(" - ");
            builder.degree(parts[0].trim());
            if (parts.length > 1) builder.institutionName(parts[1].trim());
        }

        // Extract graduation date
        String dateText = extractDateRange(eduEntry);
        if (dateText != null) {
            parseEducationDates(builder, dateText);
        }

        return builder.build();
    }

    private ProjectData parseProject(String projectEntry) {
        String[] lines = projectEntry.split("\n");
        if (lines.length < 1) return null;

        ProjectData.ProjectDataBuilder builder = ProjectData.builder();
        
        String firstLine = lines[0].trim();
        builder.name(firstLine);
        
        StringBuilder description = new StringBuilder();
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.length() > 5) {
                description.append(line).append(" ");
            }
        }
        
        if (description.length() > 0) {
            builder.description(description.toString().trim());
        }

        return builder.build();
    }

    private SkillData createSkillData(String skillName) {
        return SkillData.builder()
                .name(skillName.trim())
                .category(Skill.SkillCategory.categorizeSkill(skillName))
                .proficiencyLevel(Skill.ProficiencyLevel.INTERMEDIATE) // Default
                .matchedFromText(true)
                .build();
    }

    private Set<String> parseSkillsFromSection(String skillsSection) {
        Set<String> skills = new LinkedHashSet<>();
        
        String[] lines = skillsSection.split("\n");
        for (String line : lines) {
            String[] skillParts = line.split("[,;•\\-]");
            for (String skill : skillParts) {
                String cleanSkill = skill.trim().replaceAll("[^a-zA-Z0-9\\s\\.\\+#]", "");
                if (cleanSkill.length() >= 2 && cleanSkill.length() <= 30) {
                    skills.add(cleanSkill);
                }
            }
        }
        
        return skills;
    }

    private Set<String> extractCommonSkills(String text) {
        // This would contain a comprehensive list of technical skills to match against
        String[] commonSkills = {
            "Java", "Python", "JavaScript", "TypeScript", "C++", "C#", "PHP", "Ruby", "Go", "Rust",
            "React", "Angular", "Vue.js", "Node.js", "Express", "Spring", "Django", "Laravel",
            "MySQL", "PostgreSQL", "MongoDB", "Redis", "Oracle", "SQL Server",
            "AWS", "Azure", "GCP", "Docker", "Kubernetes", "Jenkins", "Git", "Linux"
            // Add more skills as needed
        };
        
        Set<String> foundSkills = new LinkedHashSet<>();
        String lowerText = text.toLowerCase();
        
        for (String skill : commonSkills) {
            if (lowerText.contains(skill.toLowerCase())) {
                foundSkills.add(skill);
            }
        }
        
        return foundSkills;
    }

    private String extractDateRange(String text) {
        Pattern dateRangePattern = Pattern.compile(
                "(\\d{1,2}/\\d{4}|\\w{3,9}\\s+\\d{4}|\\d{4})\\s*[-–—]\\s*(present|current|\\d{1,2}/\\d{4}|\\w{3,9}\\s+\\d{4}|\\d{4})",
                Pattern.CASE_INSENSITIVE
        );
        
        Matcher matcher = dateRangePattern.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        
        return null;
    }

    private void parseWorkExperienceDates(WorkExperienceData.WorkExperienceDataBuilder builder, String dateText) {
        // Simplified date parsing - in production, use more robust date parsing
        if (dateText.toLowerCase().contains("present") || dateText.toLowerCase().contains("current")) {
            builder.isCurrent(true);
        }
        
        // Extract start and end dates - this is a simplified implementation
        String[] parts = dateText.split("[-–—]");
        if (parts.length >= 2) {
            try {
                LocalDate startDate = parseFlexibleDate(parts[0].trim());
                if (startDate != null) {
                    builder.startDate(startDate);
                }
                
                if (!builder.build().getIsCurrent()) {
                    LocalDate endDate = parseFlexibleDate(parts[1].trim());
                    if (endDate != null) {
                        builder.endDate(endDate);
                    }
                }
            } catch (Exception e) {
                log.debug("Error parsing work experience dates: {}", e.getMessage());
            }
        }
    }

    private void parseEducationDates(EducationData.EducationDataBuilder builder, String dateText) {
        // Similar to work experience date parsing
        String[] parts = dateText.split("[-–—]");
        if (parts.length >= 2) {
            try {
                LocalDate startDate = parseFlexibleDate(parts[0].trim());
                if (startDate != null) {
                    builder.startDate(startDate);
                }
                
                LocalDate endDate = parseFlexibleDate(parts[1].trim());
                if (endDate != null) {
                    builder.endDate(endDate);
                }
            } catch (Exception e) {
                log.debug("Error parsing education dates: {}", e.getMessage());
            }
        }
    }

    private LocalDate parseFlexibleDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        
        dateStr = dateStr.trim().toLowerCase();
        
        // Skip present/current
        if (dateStr.contains("present") || dateStr.contains("current")) {
            return null;
        }
        
        // Try different date formats
        DateTimeFormatter[] formatters = {
            DateTimeFormatter.ofPattern("MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy"),
            DateTimeFormatter.ofPattern("MMM yyyy"),
            DateTimeFormatter.ofPattern("MMMM yyyy")
        };
        
        for (DateTimeFormatter formatter : formatters) {
            try {
                if (formatter.toString().contains("MM") || formatter.toString().contains("MMM")) {
                    return LocalDate.parse("01/" + dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                } else {
                    return LocalDate.parse(dateStr + "-01-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                }
            } catch (DateTimeParseException e) {
                // Try next formatter
            }
        }
        
        return null;
    }

    /**
     * Convert parsed data to JSON string.
     */
    public String toJson(ParsedResumeData parsedData) {
        try {
            return objectMapper.writeValueAsString(parsedData);
        } catch (JsonProcessingException e) {
            log.error("Error converting parsed data to JSON", e);
            return "{}";
        }
    }

    // Data classes for parsed information
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ParsedResumeData {
        private PersonalInfo personalInfo;
        private String summary;
        private List<WorkExperienceData> workExperiences;
        private List<EducationData> educations;
        private List<SkillData> skills;
        private List<ProjectData> projects;
        private Double confidenceScore;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PersonalInfo {
        private String fullName;
        private String email;
        private String phone;
        private String location;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class WorkExperienceData {
        private String jobTitle;
        private String companyName;
        private LocalDate startDate;
        private LocalDate endDate;
        private Boolean isCurrent;
        private String description;
        private String location;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EducationData {
        private String institutionName;
        private String degree;
        private String fieldOfStudy;
        private LocalDate startDate;
        private LocalDate endDate;
        private String location;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SkillData {
        private String name;
        private Skill.SkillCategory category;
        private Skill.ProficiencyLevel proficiencyLevel;
        private Boolean matchedFromText;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ProjectData {
        private String name;
        private String description;
        private String role;
        private String organization;
        private LocalDate startDate;
        private LocalDate endDate;
        private Boolean isOngoing;
    }
}