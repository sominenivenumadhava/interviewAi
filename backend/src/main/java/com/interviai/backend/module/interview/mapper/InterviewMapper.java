package com.interviai.backend.module.interview.mapper;

import com.interviai.backend.module.interview.dto.response.InterviewAnswerResponse;
import com.interviai.backend.module.interview.dto.response.InterviewQuestionResponse;
import com.interviai.backend.module.interview.dto.response.InterviewResponse;
import com.interviai.backend.module.interview.entity.Interview;
import com.interviai.backend.module.interview.entity.InterviewAnswer;
import com.interviai.backend.module.interview.entity.InterviewQuestion;

import java.util.List;

public interface InterviewMapper {
    
    InterviewResponse toResponse(Interview interview);
    
    InterviewQuestionResponse toQuestionResponse(InterviewQuestion question);
    
    List<InterviewQuestionResponse> toQuestionResponses(List<InterviewQuestion> questions);
    
    InterviewAnswerResponse toAnswerResponse(InterviewAnswer answer);
    
    List<InterviewAnswerResponse> toAnswerResponses(List<InterviewAnswer> answers);
}