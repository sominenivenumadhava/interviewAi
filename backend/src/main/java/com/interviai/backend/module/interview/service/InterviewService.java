package com.interviai.backend.module.interview.service;

import java.util.UUID;
import com.interviai.backend.common.dto.PageRequest;
import java.util.UUID;
import com.interviai.backend.common.dto.PageResponse;
import java.util.UUID;
import com.interviai.backend.module.interview.dto.request.CreateInterviewRequest;
import java.util.UUID;
import com.interviai.backend.module.interview.dto.request.UpdateInterviewStatusRequest;
import java.util.UUID;
import com.interviai.backend.module.interview.dto.response.InterviewResponse;
import java.util.UUID;
import com.interviai.backend.module.interview.dto.response.InterviewSessionResponse;

import java.util.List;
import java.util.UUID;

public interface InterviewService {
    
    InterviewResponse createInterview(CreateInterviewRequest request, UUID userId);
    
    InterviewSessionResponse startInterview(String sessionId, UUID userId);
    
    InterviewResponse updateInterviewStatus(UpdateInterviewStatusRequest request, UUID userId);
    
    InterviewResponse getInterview(UUID id, UUID userId);
    
    InterviewSessionResponse getInterviewSession(String sessionId, UUID userId);
    
    PageResponse<InterviewResponse> getUserInterviews(UUID userId, PageRequest pageRequest);
    
    List<InterviewResponse> getUpcomingInterviews(UUID userId);
    
    List<InterviewResponse> getRecentInterviews(UUID userId, int limit);
    
    void deleteInterview(UUID id, UUID userId);
    
    InterviewResponse completeInterview(String sessionId, UUID userId);
    
    InterviewResponse pauseInterview(String sessionId, UUID userId);
    
    InterviewResponse resumeInterview(String sessionId, UUID userId);
    
    InterviewResponse cancelInterview(String sessionId, UUID userId);
}
