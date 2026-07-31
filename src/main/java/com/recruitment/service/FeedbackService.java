package com.recruitment.service;

import com.recruitment.dto.FeedbackRequest;
import com.recruitment.dto.FeedbackResponse;
import com.recruitment.enums.InterviewStatus;
import com.recruitment.exception.ForbiddenException;
import com.recruitment.exception.NotFoundException;
import com.recruitment.mapper.FeedbackMapper;
import com.recruitment.model.Feedback;
import com.recruitment.model.Interview;
import com.recruitment.model.User;
import com.recruitment.repository.FeedbackRepository;
import com.recruitment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final InterviewService interviewService;
    private final FeedbackMapper feedbackMapper;

    @Transactional
    public FeedbackResponse submit(Long interviewId, String actorEmail, FeedbackRequest request) {
        Interview interview = interviewService.getEntity(interviewId);
        if (interview.getStatus() != InterviewStatus.COMPLETED) {
            throw new IllegalArgumentException("Feedback can only be given on completed interviews");
        }

        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new NotFoundException("User not found: " + actorEmail));
        boolean isInterviewer = interview.getInterviewer().getId().equals(actor.getId());
        boolean isAdmin = actor.getRoles().stream()
                .anyMatch(role -> role.getName() == com.recruitment.enums.RoleType.ADMIN);
        if (!isInterviewer && !isAdmin) {
            throw new ForbiddenException("Only the assigned interviewer or an admin can submit feedback");
        }

        Feedback feedback = Feedback.builder()
                .rating(request.rating())
                .comments(request.comments())
                .interview(interview)
                .user(actor)
                .build();
        return feedbackMapper.toResponse(feedbackRepository.save(feedback));
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponse> getByInterview(Long interviewId) {
        interviewService.getEntity(interviewId);
        return feedbackRepository.findByInterviewId(interviewId).stream()
                .map(feedbackMapper::toResponse)
                .toList();
    }
}
