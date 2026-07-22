package com.interviai.backend.module.interview.dto.request;

import com.interviai.backend.module.interview.enums.InterviewStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateInterviewStatusRequest {
    
    @NotBlank(message = "Session ID is required")
    private String sessionId;
    
    @NotNull(message = "Status is required")
    private InterviewStatus status;
    
    private String reason;
}