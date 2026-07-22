package com.interviai.backend.module.interview.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SubmitAnswerRequest {
    
    @NotBlank(message = "Session ID is required")
    private String sessionId;
    
    private Integer questionOrder;
    
    @Size(max = 10000, message = "Answer text must be less than 10000 characters")
    private String answerText;
    
    private String answerAudioUrl;
    
    private String answerVideoUrl;
    
    private Long timeTakenSeconds;
}