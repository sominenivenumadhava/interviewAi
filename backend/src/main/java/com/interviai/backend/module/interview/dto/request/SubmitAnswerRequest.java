package com.interviai.backend.module.interview.dto.request;

import com.interviai.backend.common.constant.ApiConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SubmitAnswerRequest {
    
    @NotBlank(message = "Session ID is required")
    private String sessionId;
    
    private Integer questionOrder;
    
    @Size(
            max = ApiConstants.MAX_ANSWER_LENGTH,
            message = "Answer text must be 50000 characters or fewer"
    )
    private String answerText;
    
    private String answerAudioUrl;
    
    private String answerVideoUrl;
    
    private Long timeTakenSeconds;
}
