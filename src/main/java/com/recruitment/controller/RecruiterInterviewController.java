package com.recruitment.controller;

import com.recruitment.dto.InterviewRequest;
import com.recruitment.dto.InterviewResponse;
import com.recruitment.dto.FeedbackResponse;
import com.recruitment.service.FeedbackService;
import com.recruitment.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recruiter")
@RequiredArgsConstructor
public class RecruiterInterviewController {

    private final InterviewService interviewService;
    private final FeedbackService feedbackService;

    @PostMapping("/applications/{applicationId}/interviews")
    public InterviewResponse schedule(Authentication authentication,
                                      @PathVariable Long applicationId,
                                      @Valid @RequestBody InterviewRequest request) {
        return interviewService.schedule(authentication.getName(), applicationId, request);
    }

    @GetMapping("/applications/{applicationId}/interviews")
    public List<InterviewResponse> listByApplication(@PathVariable Long applicationId) {
        return interviewService.getByApplication(applicationId);
    }

    @GetMapping("/interviews/{id}/feedbacks")
    public List<FeedbackResponse> listFeedbacks(@PathVariable Long id) {
        return feedbackService.getByInterview(id);
    }
}
