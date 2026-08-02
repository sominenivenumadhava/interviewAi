package com.interviai.backend.module.resume.controller;

import com.interviai.backend.common.dto.ApiResponse;
import com.interviai.backend.common.dto.PageResponse;
import com.interviai.backend.module.resume.dto.request.ResumeUpdateRequest;
import com.interviai.backend.module.resume.dto.request.ResumeUploadRequest;
import com.interviai.backend.module.resume.dto.response.ResumeResponse;
import com.interviai.backend.module.resume.entity.Resume;
import com.interviai.backend.module.resume.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST Controller for resume management operations.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/resumes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Resume Management", description = "APIs for resume upload, processing, and management")
@SecurityRequirement(name = "Bearer Authentication")
public class ResumeController {

    private final ResumeService resumeService;

    @Operation(
        summary = "Upload a new resume",
        description = "Upload a PDF, DOC, or DOCX resume file for processing and parsing"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Resume uploaded successfully",
            content = @Content(schema = @Schema(implementation = ResumeResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid file or request parameters"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "413",
            description = "File size exceeds maximum limit"
        )
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ResumeResponse>> uploadResume(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Resume file (PDF, DOC, DOCX)", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Resume title", required = true)
            @RequestParam("title") String title,
            @Parameter(description = "Resume description")
            @RequestParam(value = "description", required = false) String description,
            @Parameter(description = "Set as primary resume")
            @RequestParam(value = "setPrimary", defaultValue = "false") Boolean setPrimary,
            @Parameter(description = "Replace existing primary resume")
            @RequestParam(value = "replaceExisting", defaultValue = "false") Boolean replaceExisting) {

        log.info("Resume upload request from user: {}", userDetails.getUsername());

        ResumeUploadRequest request = ResumeUploadRequest.builder()
                .title(title)
                .description(description)
                .setPrimary(setPrimary)
                .replaceExisting(replaceExisting)
                .build();

        UUID userId = getUserIdFromUserDetails(userDetails);
        ResumeResponse response = resumeService.uploadResume(userId, request, file);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Resume uploaded successfully"));
    }

    @Operation(
        summary = "Get resume by ID",
        description = "Retrieve detailed information about a specific resume"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Resume retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Resume not found"
        )
    })
    @GetMapping("/{resumeId}")
    public ResponseEntity<ApiResponse<ResumeResponse>> getResumeById(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Resume ID", required = true)
            @PathVariable UUID resumeId) {

        UUID userId = getUserIdFromUserDetails(userDetails);
        Optional<ResumeResponse> resume = resumeService.getResumeById(userId, resumeId);

        return resume
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Resume retrieved successfully")))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Get user resumes",
        description = "Retrieve all resumes for the authenticated user with pagination"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Resumes retrieved successfully"
        )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ResumeResponse>>> getUserResumes(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        UUID userId = getUserIdFromUserDetails(userDetails);
        PageResponse<ResumeResponse> resumes = resumeService.getUserResumes(userId, pageable);

        return ResponseEntity.ok(ApiResponse.success(resumes, "Resumes retrieved successfully"));
    }

    @Operation(
        summary = "Get primary resume",
        description = "Retrieve the primary resume for the authenticated user"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Primary resume retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "No primary resume found"
        )
    })
    @GetMapping("/primary")
    public ResponseEntity<ApiResponse<ResumeResponse>> getPrimaryResume(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = getUserIdFromUserDetails(userDetails);
        Optional<ResumeResponse> primaryResume = resumeService.getPrimaryResume(userId);

        return primaryResume
                .map(r -> ResponseEntity.ok(ApiResponse.success(r, "Primary resume retrieved successfully")))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
        summary = "Update resume information",
        description = "Update resume metadata and personal information"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Resume updated successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Resume not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters"
        )
    })
    @PutMapping("/{resumeId}")
    public ResponseEntity<ApiResponse<ResumeResponse>> updateResume(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Resume ID", required = true)
            @PathVariable UUID resumeId,
            @Parameter(description = "Resume update information", required = true)
            @Valid @RequestBody ResumeUpdateRequest request) {

        UUID userId = getUserIdFromUserDetails(userDetails);
        ResumeResponse response = resumeService.updateResume(userId, resumeId, request);

        return ResponseEntity.ok(ApiResponse.success(response, "Resume updated successfully"));
    }

    @Operation(
        summary = "Set primary resume",
        description = "Set a specific resume as the primary resume for the user"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Primary resume set successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Resume not found"
        )
    })
    @PutMapping({"/{resumeId}/primary", "/{resumeId}/set-primary"})
    public ResponseEntity<ApiResponse<ResumeResponse>> setPrimaryResume(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Resume ID", required = true)
            @PathVariable UUID resumeId) {

        UUID userId = getUserIdFromUserDetails(userDetails);
        ResumeResponse response = resumeService.setPrimaryResume(userId, resumeId);

        return ResponseEntity.ok(ApiResponse.success(response, "Primary resume set successfully"));
    }

    @Operation(
        summary = "Delete resume",
        description = "Delete a resume and its associated file"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "204",
            description = "Resume deleted successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Resume not found"
        )
    })
    @DeleteMapping("/{resumeId}")
    public ResponseEntity<ApiResponse<Void>> deleteResume(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Resume ID", required = true)
            @PathVariable UUID resumeId) {

        UUID userId = getUserIdFromUserDetails(userDetails);
        resumeService.deleteResume(userId, resumeId);

        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Search resumes by title",
        description = "Search user's resumes by title with pagination"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Search completed successfully"
        )
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<ResumeResponse>>> searchResumesByTitle(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Search title", required = true)
            @RequestParam("title") String title,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        UUID userId = getUserIdFromUserDetails(userDetails);
        PageResponse<ResumeResponse> resumes = resumeService.searchResumesByTitle(userId, title, pageable);

        return ResponseEntity.ok(ApiResponse.success(resumes, "Search completed successfully"));
    }

    @Operation(
        summary = "Get resumes by status",
        description = "Retrieve resumes filtered by processing status"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Resumes retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid status parameter"
        )
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<ResumeResponse>>> getResumesByStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Processing status", required = true)
            @PathVariable Resume.ResumeStatus status) {

        UUID userId = getUserIdFromUserDetails(userDetails);
        List<ResumeResponse> resumes = resumeService.getResumesByStatus(userId, status);

        return ResponseEntity.ok(ApiResponse.success(resumes, "Resumes retrieved successfully"));
    }

    @Operation(
        summary = "Get processing status",
        description = "Get the current processing status of a resume"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Processing status retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Resume not found"
        )
    })
    @GetMapping("/{resumeId}/status")
    public ResponseEntity<ApiResponse<String>> getProcessingStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Resume ID", required = true)
            @PathVariable UUID resumeId) {

        UUID userId = getUserIdFromUserDetails(userDetails);
        String status = resumeService.getProcessingStatus(userId, resumeId);

        return ResponseEntity.ok(ApiResponse.success(status, "Processing status retrieved successfully"));
    }

    @Operation(
        summary = "Reprocess resume",
        description = "Trigger reprocessing of a resume"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Resume reprocessing started successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Resume not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Resume is currently being processed"
        )
    })
    @PostMapping("/{resumeId}/reprocess")
    public ResponseEntity<ApiResponse<ResumeResponse>> reprocessResume(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Resume ID", required = true)
            @PathVariable UUID resumeId) {

        UUID userId = getUserIdFromUserDetails(userDetails);
        ResumeResponse response = resumeService.reprocessResume(userId, resumeId);

        return ResponseEntity.ok(ApiResponse.success(response, "Resume reprocessing started successfully"));
    }

    @Operation(
        summary = "Download resume file",
        description = "Download the original resume file"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "File downloaded successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Resume or file not found"
        )
    })
    @GetMapping("/{resumeId}/download")
    public ResponseEntity<byte[]> downloadResumeFile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Resume ID", required = true)
            @PathVariable UUID resumeId) {

        UUID userId = getUserIdFromUserDetails(userDetails);
        byte[] fileContent = resumeService.getResumeFileContent(userId, resumeId);

        // Get resume info to set proper headers
        Optional<ResumeResponse> resumeOpt = resumeService.getResumeById(userId, resumeId);
        if (resumeOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ResumeResponse resume = resumeOpt.get();
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + resume.getOriginalFilename() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, resume.getContentType())
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileContent.length))
                .body(fileContent);
    }

    @Operation(
        summary = "Get extracted text",
        description = "Get the extracted text content from a resume"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Extracted text retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Resume not found"
        )
    })
    @GetMapping("/{resumeId}/text")
    public ResponseEntity<ApiResponse<String>> getExtractedText(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Resume ID", required = true)
            @PathVariable UUID resumeId) {

        UUID userId = getUserIdFromUserDetails(userDetails);
        String extractedText = resumeService.getResumeExtractedText(userId, resumeId);

        return ResponseEntity.ok(ApiResponse.success(extractedText, "Extracted text retrieved successfully"));
    }

    @Operation(
        summary = "Get resume statistics",
        description = "Get statistics about user's resumes"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Statistics retrieved successfully"
        )
    })
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<ResumeService.ResumeStatistics>> getResumeStatistics(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = getUserIdFromUserDetails(userDetails);
        ResumeService.ResumeStatistics statistics = resumeService.getUserResumeStatistics(userId);

        return ResponseEntity.ok(ApiResponse.success(statistics, "Statistics retrieved successfully"));
    }

    @Operation(
        summary = "Find resumes by skill",
        description = "Find resumes that mention a specific skill"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Resumes found successfully"
        )
    })
    @GetMapping("/skills/{skillName}")
    public ResponseEntity<ApiResponse<List<ResumeResponse>>> findResumesBySkill(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Skill name", required = true)
            @PathVariable String skillName) {

        UUID userId = getUserIdFromUserDetails(userDetails);
        List<ResumeResponse> resumes = resumeService.findResumesBySkill(userId, skillName);

        return ResponseEntity.ok(ApiResponse.success(resumes, "Resumes found successfully"));
    }

    @Operation(
        summary = "Find resumes by experience range",
        description = "Find resumes within a specific experience range"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Resumes found successfully"
        )
    })
    @GetMapping("/experience")
    public ResponseEntity<ApiResponse<List<ResumeResponse>>> findResumesByExperienceRange(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Minimum experience in months")
            @RequestParam(value = "minMonths", defaultValue = "0") Integer minMonths,
            @Parameter(description = "Maximum experience in months")
            @RequestParam(value = "maxMonths", defaultValue = "600") Integer maxMonths) {

        UUID userId = getUserIdFromUserDetails(userDetails);
        List<ResumeResponse> resumes = resumeService.findResumesByExperienceRange(userId, minMonths, maxMonths);

        return ResponseEntity.ok(ApiResponse.success(resumes, "Resumes found successfully"));
    }

    @Operation(
        summary = "Check upload capability",
        description = "Check if user can upload more resumes"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Upload capability checked successfully"
        )
    })
    @GetMapping("/upload-capability")
    public ResponseEntity<ApiResponse<Boolean>> checkUploadCapability(
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = getUserIdFromUserDetails(userDetails);
        boolean canUpload = resumeService.canUploadMoreResumes(userId);

        String message = canUpload ? "User can upload more resumes" : "User has reached maximum resume limit";
        return ResponseEntity.ok(ApiResponse.success(canUpload, message));
    }

    /**
     * Extract user ID from UserDetails.
     * This assumes that the User entity implements UserDetails and has an ID.
     */
    private UUID getUserIdFromUserDetails(UserDetails userDetails) {
        if (userDetails instanceof com.interviai.backend.module.user.entity.User) {
            return ((com.interviai.backend.module.user.entity.User) userDetails).getId();
        }
        throw new IllegalStateException("UserDetails is not an instance of User entity");
    }
}