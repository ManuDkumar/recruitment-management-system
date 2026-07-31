package com.recruitment.controller;

import com.recruitment.dto.FeedbackRequest;
import com.recruitment.dto.FeedbackResponse;
import com.recruitment.dto.InterviewResponse;
import com.recruitment.dto.InterviewStatusRequest;
import com.recruitment.enums.InterviewStatus;
import com.recruitment.service.FeedbackService;
import com.recruitment.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interviewer")
@RequiredArgsConstructor
public class InterviewerController {

    private final InterviewService interviewService;
    private final FeedbackService feedbackService;

    @GetMapping("/interviews/me")
    public List<InterviewResponse> myInterviews(Authentication authentication,
                                                @RequestParam(required = false) InterviewStatus status) {
        return interviewService.getMyInterviews(authentication.getName(), status);
    }

    @PatchMapping("/interviews/{id}/status")
    public InterviewResponse updateStatus(Authentication authentication,
                                          @PathVariable Long id,
                                          @Valid @RequestBody InterviewStatusRequest request) {
        return interviewService.updateStatus(authentication.getName(), id, request);
    }

    @GetMapping("/interviews/{id}/feedbacks")
    public List<FeedbackResponse> feedbacks(@PathVariable Long id) {
        return feedbackService.getByInterview(id);
    }

    @PostMapping("/interviews/{id}/feedback")
    public FeedbackResponse submitFeedback(Authentication authentication,
                                           @PathVariable Long id,
                                           @Valid @RequestBody FeedbackRequest request) {
        return feedbackService.submit(id, authentication.getName(), request);
    }
}
