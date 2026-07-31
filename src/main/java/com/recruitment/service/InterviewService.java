package com.recruitment.service;

import com.recruitment.dto.InterviewRequest;
import com.recruitment.dto.InterviewResponse;
import com.recruitment.dto.InterviewStatusRequest;
import com.recruitment.enums.ApplicationStatus;
import com.recruitment.enums.InterviewStatus;
import com.recruitment.enums.InterviewType;
import com.recruitment.enums.RoleType;
import com.recruitment.exception.ForbiddenException;
import com.recruitment.exception.NotFoundException;
import com.recruitment.mapper.InterviewMapper;
import com.recruitment.model.Application;
import com.recruitment.model.Interview;
import com.recruitment.model.User;
import com.recruitment.repository.ApplicationRepository;
import com.recruitment.repository.InterviewRepository;
import com.recruitment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private static final Set<ApplicationStatus> SCHEDULABLE_STATUSES =
            Set.of(ApplicationStatus.SCREENING, ApplicationStatus.TECHNICAL_INTERVIEW, ApplicationStatus.HR_INTERVIEW);

    private final InterviewRepository interviewRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final InterviewMapper interviewMapper;

    @Transactional
    public InterviewResponse schedule(String actorEmail, Long applicationId, InterviewRequest request) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found: " + applicationId));
        if (!SCHEDULABLE_STATUSES.contains(application.getStatus())) {
            throw new IllegalArgumentException(
                    "Interviews can only be scheduled for active pipeline applications");
        }

        User interviewer = userRepository.findByEmail(request.interviewerEmail())
                .orElseThrow(() -> new NotFoundException("Interviewer not found: " + request.interviewerEmail()));
        if (interviewer.getRoles().stream().noneMatch(role -> role.getName() == RoleType.INTERVIEWER)) {
            throw new IllegalArgumentException("User is not an interviewer: " + request.interviewerEmail());
        }

        advancePipeline(application, request.type());

        Interview interview = Interview.builder()
                .scheduledAt(request.scheduledAt())
                .type(request.type())
                .status(InterviewStatus.SCHEDULED)
                .application(application)
                .interviewer(interviewer)
                .build();
        return interviewMapper.toResponse(interviewRepository.save(interview));
    }

    @Transactional(readOnly = true)
    public List<InterviewResponse> getByApplication(Long applicationId) {
        if (!applicationRepository.existsById(applicationId)) {
            throw new NotFoundException("Application not found: " + applicationId);
        }
        return interviewRepository.findByApplicationId(applicationId).stream()
                .map(interviewMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InterviewResponse> getMyInterviews(String interviewerEmail, InterviewStatus status) {
        User interviewer = userRepository.findByEmail(interviewerEmail)
                .orElseThrow(() -> new NotFoundException("Interviewer not found: " + interviewerEmail));
        return interviewRepository.findByInterviewerId(interviewer.getId()).stream()
                .filter(interview -> status == null || interview.getStatus() == status)
                .map(interviewMapper::toResponse)
                .toList();
    }

    @Transactional
    public InterviewResponse updateStatus(String actorEmail, Long interviewId, InterviewStatusRequest request) {
        Interview interview = getEntity(interviewId);
        ensureAllowed(actorEmail, interview, "Only the assigned interviewer or an admin can change interview status");
        if (interview.getStatus() != InterviewStatus.SCHEDULED) {
            throw new IllegalArgumentException(
                    "Only SCHEDULED interviews can be changed, current status is " + interview.getStatus());
        }
        if (request.status() != InterviewStatus.COMPLETED && request.status() != InterviewStatus.CANCELLED) {
            throw new IllegalArgumentException("Interview status can only be set to COMPLETED or CANCELLED");
        }
        interview.setStatus(request.status());
        return interviewMapper.toResponse(interviewRepository.save(interview));
    }

    @Transactional(readOnly = true)
    public Interview getEntity(Long id) {
        return interviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Interview not found: " + id));
    }

    private void ensureAllowed(String actorEmail, Interview interview, String message) {
        User actor = userRepository.findByEmail(actorEmail)
                .orElseThrow(() -> new NotFoundException("User not found: " + actorEmail));
        boolean isAdmin = actor.getRoles().stream().anyMatch(role -> role.getName() == RoleType.ADMIN);
        boolean isInterviewer = interview.getInterviewer().getId().equals(actor.getId());
        if (!isAdmin && !isInterviewer) {
            throw new ForbiddenException(message);
        }
    }

    private void advancePipeline(Application application, InterviewType type) {
        ApplicationStatus current = application.getStatus();
        if (type == InterviewType.TECHNICAL && current == ApplicationStatus.SCREENING) {
            application.setStatus(ApplicationStatus.TECHNICAL_INTERVIEW);
            application.setUpdatedAt(java.time.LocalDateTime.now());
            applicationRepository.save(application);
        } else if (type == InterviewType.HR
                && (current == ApplicationStatus.SCREENING || current == ApplicationStatus.TECHNICAL_INTERVIEW)) {
            application.setStatus(ApplicationStatus.HR_INTERVIEW);
            application.setUpdatedAt(java.time.LocalDateTime.now());
            applicationRepository.save(application);
        }
    }
}
