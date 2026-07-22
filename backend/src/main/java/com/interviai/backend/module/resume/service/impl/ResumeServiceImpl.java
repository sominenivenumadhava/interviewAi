package com.interviai.backend.module.resume.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviai.backend.common.dto.PageResponse;
import com.interviai.backend.module.ai.service.AIService;
import com.interviai.backend.module.resume.dto.request.ResumeUpdateRequest;
import com.interviai.backend.module.resume.dto.request.ResumeUploadRequest;
import com.interviai.backend.module.resume.dto.response.ResumeResponse;
import com.interviai.backend.module.resume.entity.*;
import com.interviai.backend.module.resume.exception.InvalidResumeFormatException;
import com.interviai.backend.module.resume.exception.ResumeNotFoundException;
import com.interviai.backend.module.resume.exception.ResumeProcessingException;
import com.interviai.backend.module.resume.mapper.ResumeMapper;
import com.interviai.backend.module.resume.repository.ResumeRepository;
import com.interviai.backend.module.resume.service.ResumeService;
import com.interviai.backend.module.resume.util.PdfTextExtractor;
import com.interviai.backend.module.resume.util.ResumeParser;
import com.interviai.backend.module.resume.validator.ResumeValidator;
import com.interviai.backend.module.user.entity.User;
import com.interviai.backend.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation for resume management operations.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserService userService;
    private final ResumeValidator resumeValidator;
    private final ResumeMapper resumeMapper;
    private final PdfTextExtractor pdfTextExtractor;
    private final ResumeParser resumeParser;
    
    @Autowired
    private AIService aiService;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.resume.max-per-user:10}")
    private int maxResumesPerUser;

    @Value("${app.resume.upload-dir:uploads/resumes}")
    private String uploadDirectory;

    @Value("${app.resume.processing-timeout-minutes:30}")
    private int processingTimeoutMinutes;

    @Override
    public ResumeResponse uploadResume(UUID userId, ResumeUploadRequest request, MultipartFile file) {
        log.info("Starting resume upload for user: {}", userId);

        // Validate request and file
        List<String> validationErrors = resumeValidator.validateResumeUpload(request, file);
        if (!validationErrors.isEmpty()) {
            throw new InvalidResumeFormatException("Validation failed: " + String.join(", ", validationErrors));
        }

        // Get user
        User user = userService.getUserById(userId);

        // Check if user can upload more resumes
        if (!canUploadMoreResumes(userId)) {
            throw new ResumeProcessingException("Maximum number of resumes reached for user");
        }

        try {
            // Create resume entity
            Resume resume = createResumeEntity(user, request, file);

            // Save file to disk
            String filePath = saveResumeFile(resume.getId(), file);
            resume.setFilePath(filePath);

            // Save resume to database
            resume = resumeRepository.save(resume);

            // Handle primary resume logic
            if (Boolean.TRUE.equals(request.getSetPrimary()) || !resumeRepository.existsByUserAndIsPrimaryTrue(user)) {
                setPrimaryResumeInternal(user, resume.getId());
            }

            // Process resume asynchronously
            processResumeAsync(resume.getId());

            log.info("Resume uploaded successfully for user: {}, resumeId: {}", userId, resume.getId());
            return resumeMapper.toResumeResponseBasic(resume);

        } catch (IOException e) {
            log.error("Error saving resume file for user: {}", userId, e);
            throw new ResumeProcessingException("Failed to save resume file", e);
        } catch (Exception e) {
            log.error("Error uploading resume for user: {}", userId, e);
            if (e instanceof InvalidResumeFormatException || e instanceof ResumeProcessingException) {
                throw e;
            }
            throw new ResumeProcessingException("Failed to upload resume", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ResumeResponse> getResumeById(UUID userId, UUID resumeId) {
        log.debug("Getting resume by ID: {} for user: {}", resumeId, userId);

        User user = userService.getUserById(userId);
        Optional<Resume> resumeOpt = resumeRepository.findByIdAndUser(resumeId, user);

        return resumeOpt.map(resumeMapper::toResumeResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Resume> getResumeEntityById(UUID userId, UUID resumeId) {
        User user = userService.getUserById(userId);
        return resumeRepository.findByIdAndUser(resumeId, user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResumeResponse> getUserResumes(UUID userId) {
        log.debug("Getting all resumes for user: {}", userId);

        User user = userService.getUserById(userId);
        List<Resume> resumes = resumeRepository.findByUserOrderByCreatedAtDesc(user);

        return resumeMapper.toResumeResponseList(resumes);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ResumeResponse> getUserResumes(UUID userId, Pageable pageable) {
        log.debug("Getting resumes for user: {} with pagination", userId);

        User user = userService.getUserById(userId);
        Page<Resume> resumePage = resumeRepository.findByUserOrderByCreatedAtDesc(user, pageable);

        List<ResumeResponse> content = resumePage.getContent().stream()
                .map(resumeMapper::toResumeResponseBasic)
                .collect(Collectors.toList());

        return PageResponse.<ResumeResponse>builder()
                .content(content)
                .page(resumePage.getNumber())
                .size(resumePage.getSize())
                .totalElements(resumePage.getTotalElements())
                .totalPages(resumePage.getTotalPages())
                .first(resumePage.isFirst())
                .last(resumePage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ResumeResponse> getPrimaryResume(UUID userId) {
        log.debug("Getting primary resume for user: {}", userId);

        User user = userService.getUserById(userId);
        Optional<Resume> primaryResume = resumeRepository.findByUserAndIsPrimaryTrue(user);

        return primaryResume.map(resumeMapper::toResumeResponse);
    }

    @Override
    public ResumeResponse updateResume(UUID userId, UUID resumeId, ResumeUpdateRequest request) {
        log.info("Updating resume: {} for user: {}", resumeId, userId);

        // Validate request
        List<String> validationErrors = resumeValidator.validateResumeUpdate(request);
        if (!validationErrors.isEmpty()) {
            throw new InvalidResumeFormatException("Validation failed: " + String.join(", ", validationErrors));
        }

        User user = userService.getUserById(userId);
        Resume resume = resumeRepository.findByIdAndUser(resumeId, user)
                .orElseThrow(() -> new ResumeNotFoundException(resumeId, userId));

        // Update fields if provided
        if (request.getTitle() != null) {
            resume.setTitle(request.getTitle());
        }
        if (request.getSummary() != null) {
            resume.setSummary(request.getSummary());
        }
        if (request.getFullName() != null) {
            resume.setFullName(request.getFullName());
        }
        if (request.getEmail() != null) {
            resume.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            resume.setPhone(request.getPhone());
        }
        if (request.getLocation() != null) {
            resume.setLocation(request.getLocation());
        }
        if (request.getTotalExperienceYears() != null) {
            resume.setTotalExperienceYears(request.getTotalExperienceYears());
        }
        if (request.getTotalExperienceMonths() != null) {
            resume.setTotalExperienceMonths(request.getTotalExperienceMonths());
        }

        // Handle primary resume change
        if (Boolean.TRUE.equals(request.getIsPrimary())) {
            setPrimaryResumeInternal(user, resumeId);
        } else if (Boolean.FALSE.equals(request.getIsPrimary()) && resume.getIsPrimary()) {
            resume.unsetPrimary();
        }

        resume = resumeRepository.save(resume);

        log.info("Resume updated successfully: {}", resumeId);
        return resumeMapper.toResumeResponse(resume);
    }

    @Override
    public ResumeResponse setPrimaryResume(UUID userId, UUID resumeId) {
        log.info("Setting primary resume: {} for user: {}", resumeId, userId);

        User user = userService.getUserById(userId);
        Resume resume = resumeRepository.findByIdAndUser(resumeId, user)
                .orElseThrow(() -> new ResumeNotFoundException(resumeId, userId));

        setPrimaryResumeInternal(user, resumeId);

        resume = resumeRepository.findById(resumeId).orElseThrow();
        return resumeMapper.toResumeResponse(resume);
    }

    @Override
    public void deleteResume(UUID userId, UUID resumeId) {
        log.info("Deleting resume: {} for user: {}", resumeId, userId);

        User user = userService.getUserById(userId);
        Resume resume = resumeRepository.findByIdAndUser(resumeId, user)
                .orElseThrow(() -> new ResumeNotFoundException(resumeId, userId));

        // Delete physical file
        try {
            if (resume.getFilePath() != null) {
                Path filePath = Paths.get(resume.getFilePath());
                Files.deleteIfExists(filePath);
            }
        } catch (IOException e) {
            log.warn("Could not delete resume file: {}", resume.getFilePath(), e);
        }

        // Delete from database
        resumeRepository.delete(resume);

        log.info("Resume deleted successfully: {}", resumeId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ResumeResponse> searchResumesByTitle(UUID userId, String title, Pageable pageable) {
        log.debug("Searching resumes by title: '{}' for user: {}", title, userId);

        User user = userService.getUserById(userId);
        Page<Resume> resumePage = resumeRepository.findByUserAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(
                user, title, pageable);

        List<ResumeResponse> content = resumePage.getContent().stream()
                .map(resumeMapper::toResumeResponseBasic)
                .collect(Collectors.toList());

        return PageResponse.<ResumeResponse>builder()
                .content(content)
                .page(resumePage.getNumber())
                .size(resumePage.getSize())
                .totalElements(resumePage.getTotalElements())
                .totalPages(resumePage.getTotalPages())
                .first(resumePage.isFirst())
                .last(resumePage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResumeResponse> getResumesByStatus(UUID userId, Resume.ResumeStatus status) {
        log.debug("Getting resumes by status: {} for user: {}", status, userId);

        User user = userService.getUserById(userId);
        List<Resume> resumes = resumeRepository.findByUserAndStatusOrderByCreatedAtDesc(user, status);

        return resumeMapper.toResumeResponseList(resumes);
    }

    @Override
    @Transactional(readOnly = true)
    public String getProcessingStatus(UUID userId, UUID resumeId) {
        User user = userService.getUserById(userId);
        Resume resume = resumeRepository.findByIdAndUser(resumeId, user)
                .orElseThrow(() -> new ResumeNotFoundException(resumeId, userId));

        return resume.getStatus().getDisplayName();
    }

    @Override
    public ResumeResponse reprocessResume(UUID userId, UUID resumeId) {
        log.info("Reprocessing resume: {} for user: {}", resumeId, userId);

        User user = userService.getUserById(userId);
        Resume resume = resumeRepository.findByIdAndUser(resumeId, user)
                .orElseThrow(() -> new ResumeNotFoundException(resumeId, userId));

        if (resume.isProcessing()) {
            throw new ResumeProcessingException("Resume is currently being processed");
        }

        // Reset processing status
        resume.startProcessing();
        resumeRepository.save(resume);

        // Process asynchronously
        processResumeAsync(resumeId);

        return resumeMapper.toResumeResponseBasic(resume);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getResumeFileContent(UUID userId, UUID resumeId) {
        User user = userService.getUserById(userId);
        Resume resume = resumeRepository.findByIdAndUser(resumeId, user)
                .orElseThrow(() -> new ResumeNotFoundException(resumeId, userId));

        if (resume.getFilePath() == null) {
            throw new ResumeNotFoundException("Resume file not found");
        }

        try {
            return Files.readAllBytes(Paths.get(resume.getFilePath()));
        } catch (IOException e) {
            log.error("Error reading resume file: {}", resume.getFilePath(), e);
            throw new ResumeProcessingException("Failed to read resume file", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String getResumeExtractedText(UUID userId, UUID resumeId) {
        User user = userService.getUserById(userId);
        Resume resume = resumeRepository.findByIdAndUser(resumeId, user)
                .orElseThrow(() -> new ResumeNotFoundException(resumeId, userId));

        return resume.getExtractedText();
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeStatistics getUserResumeStatistics(UUID userId) {
        log.debug("Getting resume statistics for user: {}", userId);

        User user = userService.getUserById(userId);
        
        // Get basic stats
        long totalResumes = resumeRepository.countByUser(user);
        long completedResumes = resumeRepository.countByUserAndStatus(user, Resume.ResumeStatus.COMPLETED);
        long processingResumes = resumeRepository.countByUserAndStatus(user, Resume.ResumeStatus.PROCESSING);
        long failedResumes = resumeRepository.countByUserAndStatus(user, Resume.ResumeStatus.FAILED);
        long primaryResumes = resumeRepository.existsByUserAndIsPrimaryTrue(user) ? 1L : 0L;

        // Get additional stats from completed resumes
        List<Resume> completedResumesList = resumeRepository.findByUserAndStatusOrderByCreatedAtDesc(
                user, Resume.ResumeStatus.COMPLETED);

        double averageConfidenceScore = completedResumesList.stream()
                .filter(r -> r.getParsingConfidenceScore() != null)
                .mapToDouble(Resume::getParsingConfidenceScore)
                .average()
                .orElse(0.0);

        int totalExperienceMonths = completedResumesList.stream()
                .mapToInt(Resume::getTotalExperienceInMonths)
                .max()
                .orElse(0);

        int totalSkills = completedResumesList.stream()
                .mapToInt(r -> r.getSkills().size())
                .sum();

        int totalProjects = completedResumesList.stream()
                .mapToInt(r -> r.getProjects().size())
                .sum();

        return new ResumeStatistics(
                totalResumes,
                completedResumes,
                processingResumes,
                failedResumes,
                primaryResumes,
                averageConfidenceScore,
                totalExperienceMonths,
                totalSkills,
                totalProjects
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResumeResponse> getHighQualityResumes(UUID userId, Double minConfidence) {
        log.debug("Getting high quality resumes for user: {} with min confidence: {}", userId, minConfidence);

        User user = userService.getUserById(userId);
        List<Resume> resumes = resumeRepository.findHighQualityResumes(user, minConfidence);

        return resumeMapper.toResumeResponseList(resumes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResumeResponse> findResumesBySkill(UUID userId, String skillName) {
        log.debug("Finding resumes by skill: '{}' for user: {}", skillName, userId);

        User user = userService.getUserById(userId);
        List<Resume> resumes = resumeRepository.findBySkillMentioned(user, skillName);

        return resumeMapper.toResumeResponseList(resumes);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResumeResponse> findResumesByExperienceRange(UUID userId, Integer minMonths, Integer maxMonths) {
        log.debug("Finding resumes by experience range: {}-{} months for user: {}", minMonths, maxMonths, userId);

        User user = userService.getUserById(userId);
        List<Resume> resumes = resumeRepository.findByExperienceRange(user, minMonths, maxMonths);

        return resumeMapper.toResumeResponseList(resumes);
    }

    @Override
    @Async
    public void processResumeAsync(UUID resumeId) {
        log.info("Starting async processing for resume: {}", resumeId);

        try {
            Resume resume = resumeRepository.findById(resumeId)
                    .orElseThrow(() -> new ResumeNotFoundException(resumeId));

            // Extract text from file
            String extractedText = extractTextFromResume(resume);
            
            // Parse extracted text using traditional parser first
            ResumeParser.ParsedResumeData parsedData = resumeParser.parseResumeText(extractedText);

            // Enhance with AI parsing
            enhanceWithAIParsing(resume, extractedText, parsedData);

            // Update resume with extracted and parsed data
            updateResumeWithParsedData(resume, extractedText, parsedData);

            // Create related entities
            createRelatedEntities(resume, parsedData);

            // Extract skills using AI
            extractAndSaveSkillsWithAI(resume, extractedText);

            // Mark as completed
            resume.completeProcessing(parsedData.getConfidenceScore());
            resumeRepository.save(resume);

            log.info("Resume processing completed successfully for: {}", resumeId);

        } catch (Exception e) {
            log.error("Error processing resume: {}", resumeId, e);
            
            try {
                Resume resume = resumeRepository.findById(resumeId).orElse(null);
                if (resume != null) {
                    resume.failProcessing(e.getMessage());
                    resumeRepository.save(resume);
                }
            } catch (Exception ex) {
                log.error("Error updating resume failure status: {}", resumeId, ex);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUploadMoreResumes(UUID userId) {
        User user = userService.getUserById(userId);
        long currentCount = resumeRepository.countByUser(user);
        return currentCount < maxResumesPerUser;
    }

    @Override
    public int getMaxResumesPerUser() {
        return maxResumesPerUser;
    }

    @Override
    @Transactional
    public void cleanupOldFailedResumes() {
        log.info("Cleaning up old failed resumes");

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7); // Keep failed resumes for 7 days
        resumeRepository.deleteOldFailedResumes(cutoffDate);

        log.info("Old failed resumes cleanup completed");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Resume> getStuckProcessingResumes() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(processingTimeoutMinutes);
        return resumeRepository.findStuckProcessingResumes(cutoffTime);
    }

    // Private helper methods

    private Resume createResumeEntity(User user, ResumeUploadRequest request, MultipartFile file) {
        return Resume.builder()
                .user(user)
                .title(request.getTitle())
                .originalFilename(file.getOriginalFilename())
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .status(Resume.ResumeStatus.UPLOADED)
                .isPrimary(false)
                .build();
    }

    private String saveResumeFile(UUID resumeId, MultipartFile file) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadDir = Paths.get(uploadDirectory);
        Files.createDirectories(uploadDir);

        // Generate unique filename
        String filename = resumeId.toString() + "_" + file.getOriginalFilename();
        Path filePath = uploadDir.resolve(filename);

        // Save file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }

    private void setPrimaryResumeInternal(User user, UUID resumeId) {
        // Unset all primary flags for user
        resumeRepository.unsetPrimaryForAllUserResumes(user);
        
        // Set new primary
        resumeRepository.setPrimaryResume(resumeId);
    }

    private String extractTextFromResume(Resume resume) {
        try {
            byte[] fileContent = Files.readAllBytes(Paths.get(resume.getFilePath()));
            
            if ("application/pdf".equals(resume.getContentType())) {
                return pdfTextExtractor.extractText(fileContent);
            } else {
                // For DOC/DOCX files, you would need additional libraries like Apache POI
                // For now, return empty string or implement DOC/DOCX extraction
                log.warn("DOC/DOCX extraction not implemented yet for resume: {}", resume.getId());
                return "";
            }
        } catch (Exception e) {
            log.error("Error extracting text from resume: {}", resume.getId(), e);
            throw new ResumeProcessingException("Text extraction failed", e);
        }
    }

    private void updateResumeWithParsedData(Resume resume, String extractedText, ResumeParser.ParsedResumeData parsedData) {
        resume.setExtractedText(extractedText);
        resume.setParsedContent(resumeParser.toJson(parsedData));

        if (parsedData.getPersonalInfo() != null) {
            ResumeParser.PersonalInfo personalInfo = parsedData.getPersonalInfo();
            if (personalInfo.getFullName() != null) {
                resume.setFullName(personalInfo.getFullName());
            }
            if (personalInfo.getEmail() != null) {
                resume.setEmail(personalInfo.getEmail());
            }
            if (personalInfo.getPhone() != null) {
                resume.setPhone(personalInfo.getPhone());
            }
            if (personalInfo.getLocation() != null) {
                resume.setLocation(personalInfo.getLocation());
            }
        }

        if (parsedData.getSummary() != null) {
            resume.setSummary(parsedData.getSummary());
        }

        // Calculate total experience from work experiences
        if (parsedData.getWorkExperiences() != null && !parsedData.getWorkExperiences().isEmpty()) {
            int totalMonths = parsedData.getWorkExperiences().stream()
                    .mapToInt(exp -> calculateExperienceMonths(exp))
                    .sum();
            
            resume.setTotalExperienceYears(totalMonths / 12);
            resume.setTotalExperienceMonths(totalMonths % 12);
        }
    }

    private void createRelatedEntities(Resume resume, ResumeParser.ParsedResumeData parsedData) {
        // Create education entities
        if (parsedData.getEducations() != null) {
            int order = 0;
            for (ResumeParser.EducationData eduData : parsedData.getEducations()) {
                Education education = createEducationFromData(eduData, order++);
                resume.addEducation(education);
            }
        }

        // Create work experience entities
        if (parsedData.getWorkExperiences() != null) {
            int order = 0;
            for (ResumeParser.WorkExperienceData expData : parsedData.getWorkExperiences()) {
                WorkExperience workExp = createWorkExperienceFromData(expData, order++);
                resume.addWorkExperience(workExp);
            }
        }

        // Create skill entities
        if (parsedData.getSkills() != null) {
            int order = 0;
            for (ResumeParser.SkillData skillData : parsedData.getSkills()) {
                Skill skill = createSkillFromData(skillData, order++);
                resume.addSkill(skill);
            }
        }

        // Create project entities
        if (parsedData.getProjects() != null) {
            int order = 0;
            for (ResumeParser.ProjectData projectData : parsedData.getProjects()) {
                Project project = createProjectFromData(projectData, order++);
                resume.addProject(project);
            }
        }
    }

    private Education createEducationFromData(ResumeParser.EducationData eduData, int order) {
        Education.DegreeType degreeType = Education.DegreeType.OTHER;
        if (eduData.getDegree() != null) {
            degreeType = Education.DegreeType.fromString(eduData.getDegree());
        }

        return Education.builder()
                .institutionName(eduData.getInstitutionName())
                .degree(eduData.getDegree())
                .fieldOfStudy(eduData.getFieldOfStudy())
                .degreeType(degreeType)
                .startDate(eduData.getStartDate())
                .endDate(eduData.getEndDate())
                .location(eduData.getLocation())
                .displayOrder(order)
                .build();
    }

    private WorkExperience createWorkExperienceFromData(ResumeParser.WorkExperienceData expData, int order) {
        return WorkExperience.builder()
                .companyName(expData.getCompanyName())
                .jobTitle(expData.getJobTitle())
                .startDate(expData.getStartDate())
                .endDate(expData.getEndDate())
                .isCurrent(Boolean.TRUE.equals(expData.getIsCurrent()))
                .description(expData.getDescription())
                .location(expData.getLocation())
                .displayOrder(order)
                .build();
    }

    private Skill createSkillFromData(ResumeParser.SkillData skillData, int order) {
        return Skill.builder()
                .name(skillData.getName())
                .category(skillData.getCategory())
                .proficiencyLevel(skillData.getProficiencyLevel())
                .matchedFromText(Boolean.TRUE.equals(skillData.getMatchedFromText()))
                .displayOrder(order)
                .build();
    }

    private Project createProjectFromData(ResumeParser.ProjectData projectData, int order) {
        return Project.builder()
                .name(projectData.getName())
                .description(projectData.getDescription())
                .role(projectData.getRole())
                .organization(projectData.getOrganization())
                .startDate(projectData.getStartDate())
                .endDate(projectData.getEndDate())
                .isOngoing(Boolean.TRUE.equals(projectData.getIsOngoing()))
                .displayOrder(order)
                .build();
    }

    private int calculateExperienceMonths(ResumeParser.WorkExperienceData exp) {
        if (exp.getStartDate() == null) {
            return 0;
        }
        
        var endDate = exp.getEndDate() != null ? exp.getEndDate() : 
                     (Boolean.TRUE.equals(exp.getIsCurrent()) ? java.time.LocalDate.now() : exp.getStartDate());
        
        return (int) java.time.Period.between(exp.getStartDate(), endDate).toTotalMonths();
    }
    
    private void enhanceWithAIParsing(Resume resume, String extractedText, ResumeParser.ParsedResumeData parsedData) {
        try {
            String aiPrompt = 
                "Extract the following information from this resume:\n" +
                "1. Full name\n" +
                "2. Email\n" +
                "3. Phone number\n" +
                "4. Location (city, state/country)\n" +
                "5. LinkedIn URL\n" +
                "6. GitHub URL\n" +
                "7. Portfolio/Website URL\n" +
                "8. Professional summary (if present)\n" +
                "9. Total years of experience\n" +
                "10. Current or most recent job title\n" +
                "11. Key technical skills (list)\n" +
                "12. Soft skills (list)\n" +
                "13. Educational qualifications\n" +
                "14. Certifications\n" +
                "15. Notable achievements\n\n" +
                "Resume text:\n" + extractedText + "\n\n" +
                "Return the response in JSON format with these exact field names: " +
                "fullName, email, phone, location, linkedInUrl, githubUrl, portfolioUrl, " +
                "summary, totalExperience, currentTitle, technicalSkills, softSkills, " +
                "education, certifications, achievements";

            String aiResponse = aiService.generateStructuredContent(extractedText, aiPrompt).block();
            
            if (aiResponse != null) {
                try {
                    Map<String, Object> aiData = objectMapper.readValue(aiResponse, Map.class);
                    
                    // Merge AI data with parsed data
                    mergeAIDataWithParsed(parsedData, aiData);
                    
                } catch (Exception e) {
                    log.warn("Failed to parse AI response for resume {}: {}", resume.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error enhancing resume with AI: {}", e.getMessage());
            // Continue with traditional parsing if AI fails
        }
    }
    
    private void mergeAIDataWithParsed(ResumeParser.ParsedResumeData parsedData, Map<String, Object> aiData) {
        // Merge contact information
        if (parsedData.getPersonalInfo() == null) {
            parsedData.setPersonalInfo(new ResumeParser.PersonalInfo());
        }
        if (parsedData.getPersonalInfo().getFullName() == null && aiData.containsKey("fullName")) {
            parsedData.getPersonalInfo().setFullName((String) aiData.get("fullName"));
        }
        if (parsedData.getPersonalInfo().getEmail() == null && aiData.containsKey("email")) {
            parsedData.getPersonalInfo().setEmail((String) aiData.get("email"));
        }
        if (parsedData.getPersonalInfo().getPhone() == null && aiData.containsKey("phone")) {
            parsedData.getPersonalInfo().setPhone((String) aiData.get("phone"));
        }
        
        // Merge summary
        if (parsedData.getSummary() == null && aiData.containsKey("summary")) {
            parsedData.setSummary((String) aiData.get("summary"));
        }
        
        // Merge skills
        if (aiData.containsKey("technicalSkills")) {
            List<String> aiSkills = (List<String>) aiData.get("technicalSkills");
            if (parsedData.getSkills() == null) {
                parsedData.setSkills(new ArrayList<>());
            }
            // Add unique AI-extracted skills
            for (String skill : aiSkills) {
                boolean exists = parsedData.getSkills().stream()
                        .anyMatch(s -> s.getName().equalsIgnoreCase(skill));
                if (!exists) {
                    parsedData.getSkills().add(ResumeParser.SkillData.builder()
                            .name(skill)
                            .category(Skill.SkillCategory.categorizeSkill(skill))
                            .proficiencyLevel(Skill.ProficiencyLevel.INTERMEDIATE)
                            .matchedFromText(true)
                            .build());
                }
            }
        }
        
        // Update confidence score based on AI enhancement
        double currentConfidence = parsedData.getConfidenceScore();
        parsedData.setConfidenceScore(Math.min(100.0, currentConfidence + 10.0));
    }
    
    private void extractAndSaveSkillsWithAI(Resume resume, String extractedText) {
        try {
            String skillsJson = aiService.extractSkills(extractedText).block();
            
            if (skillsJson != null) {
                Map<String, Object> skillsData = objectMapper.readValue(skillsJson, Map.class);
                
                // Extract technical skills
                if (skillsData.containsKey("technicalSkills")) {
                    List<Map<String, Object>> techSkills = (List<Map<String, Object>>) skillsData.get("technicalSkills");
                    
                    for (Map<String, Object> skillCategory : techSkills) {
                        String category = (String) skillCategory.get("category");
                        List<String> skills = (List<String>) skillCategory.get("skills");
                        String proficiency = (String) skillCategory.get("proficiencyLevel");
                        
                        for (String skillName : skills) {
                            Skill skill = resume.getSkillByName(skillName);
                            if (skill == null) {
                                skill = Skill.builder()
                                        .resume(resume)
                                        .name(skillName)
                                        .category(Skill.SkillCategory.categorizeSkill(category))
                                        .proficiencyLevel(mapProficiencyLevel(proficiency))
                                        .yearsOfExperience(estimateYearsFromProficiency(proficiency))
                                        .displayOrder(resume.getSkills().size() + 1)
                                        .build();
                                resume.addSkill(skill);
                            }
                        }
                    }
                }
                
                // Extract certifications and add as skills
                if (skillsData.containsKey("certifications")) {
                    List<String> certifications = (List<String>) skillsData.get("certifications");
                    for (String cert : certifications) {
                        Skill skill = Skill.builder()
                                .resume(resume)
                                .name(cert)
                                .category(Skill.SkillCategory.OTHER)
                                .proficiencyLevel(Skill.ProficiencyLevel.EXPERT)
                                .isCertified(true)
                                .displayOrder(resume.getSkills().size() + 1)
                                .build();
                        resume.addSkill(skill);
                    }
                }
                
                resumeRepository.save(resume);
            }
        } catch (Exception e) {
            log.error("Error extracting skills with AI for resume {}: {}", resume.getId(), e.getMessage());
        }
    }
    
    private Skill.ProficiencyLevel mapProficiencyLevel(String aiProficiency) {
        if (aiProficiency == null) return Skill.ProficiencyLevel.INTERMEDIATE;
        
        return switch (aiProficiency.toUpperCase()) {
            case "EXPERT" -> Skill.ProficiencyLevel.EXPERT;
            case "ADVANCED" -> Skill.ProficiencyLevel.ADVANCED;
            case "INTERMEDIATE" -> Skill.ProficiencyLevel.INTERMEDIATE;
            case "BEGINNER" -> Skill.ProficiencyLevel.BEGINNER;
            default -> Skill.ProficiencyLevel.INTERMEDIATE;
        };
    }
    
    private Integer estimateYearsFromProficiency(String proficiency) {
        if (proficiency == null) return 2;
        
        return switch (proficiency.toUpperCase()) {
            case "EXPERT" -> 5;
            case "ADVANCED" -> 4;
            case "INTERMEDIATE" -> 2;
            case "BEGINNER" -> 1;
            default -> 2;
        };
    }
}