package com.recruitment.service;

import com.recruitment.dto.ApplicationRequest;
import com.recruitment.dto.ApplicationResponse;
import com.recruitment.dto.ApplicationStatusRequest;
import com.recruitment.enums.ApplicationStatus;
import com.recruitment.enums.JobStatus;
import com.recruitment.exception.NotFoundException;
import com.recruitment.mapper.ApplicationMapper;
import com.recruitment.model.Application;
import com.recruitment.model.Candidate;
import com.recruitment.model.JobPosting;
import com.recruitment.repository.ApplicationRepository;
import com.recruitment.repository.CandidateRepository;
import com.recruitment.repository.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationService {

    private static final Map<ApplicationStatus, Set<ApplicationStatus>> ALLOWED_TRANSITIONS = Map.of(
            ApplicationStatus.APPLIED, Set.of(ApplicationStatus.SCREENING, ApplicationStatus.REJECTED),
            ApplicationStatus.SCREENING, Set.of(ApplicationStatus.TECHNICAL_INTERVIEW, ApplicationStatus.REJECTED),
            ApplicationStatus.TECHNICAL_INTERVIEW, Set.of(ApplicationStatus.HR_INTERVIEW, ApplicationStatus.OFFERED, ApplicationStatus.REJECTED),
            ApplicationStatus.HR_INTERVIEW, Set.of(ApplicationStatus.OFFERED, ApplicationStatus.REJECTED),
            ApplicationStatus.OFFERED, Set.of(ApplicationStatus.ACCEPTED, ApplicationStatus.REJECTED),
            ApplicationStatus.ACCEPTED, Set.of(),
            ApplicationStatus.REJECTED, Set.of()
    );

    private final ApplicationRepository applicationRepository;
    private final JobPostingRepository jobPostingRepository;
    private final CandidateRepository candidateRepository;
    private final CandidateService candidateService;
    private final ApplicationMapper applicationMapper;

    @Transactional
    public ApplicationResponse apply(String email, ApplicationRequest request) {
        Candidate candidate = candidateService.getOrCreateEntity(email);
        JobPosting jobPosting = jobPostingRepository.findById(request.jobId())
                .orElseThrow(() -> new NotFoundException("Job posting not found: " + request.jobId()));
        if (jobPosting.getStatus() != JobStatus.OPEN) {
            throw new IllegalArgumentException("Job posting is not open for applications");
        }
        if (applicationRepository.existsByCandidateIdAndJobPostingId(candidate.getId(), jobPosting.getId())) {
            throw new IllegalArgumentException("You have already applied to this job");
        }

        Application application = Application.builder()
                .candidate(candidate)
                .jobPosting(jobPosting)
                .status(ApplicationStatus.APPLIED)
                .build();
        Application saved = applicationRepository.save(application);
        log.info("Candidate {} applied to job {} (applicationId={})", email, jobPosting.getId(), saved.getId());
        return applicationMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getMyApplications(String email, ApplicationStatus status) {
        return candidateRepository.findByEmail(email)
                .map(candidate -> status == null
                        ? applicationRepository.findByCandidateId(candidate.getId())
                        : applicationRepository.findByCandidateIdAndStatus(candidate.getId(), status))
                .orElse(List.of())
                .stream()
                .map(applicationMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getMyApplication(String email, Long id) {
        Application application = getEntity(id);
        if (!application.getCandidate().getEmail().equals(email)) {
            throw new NotFoundException("Application not found: " + id);
        }
        return applicationMapper.toResponse(application);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getByJob(Long jobId) {
        if (!jobPostingRepository.existsById(jobId)) {
            throw new NotFoundException("Job posting not found: " + jobId);
        }
        return applicationRepository.findByJobPostingId(jobId).stream()
                .map(applicationMapper::toResponse)
                .toList();
    }

    @Transactional
    public ApplicationResponse updateStatus(Long id, ApplicationStatusRequest request) {
        Application application = getEntity(id);
        ApplicationStatus target = request.status();
        Set<ApplicationStatus> allowed = ALLOWED_TRANSITIONS.get(application.getStatus());
        if (allowed == null || !allowed.contains(target)) {
            throw new IllegalArgumentException(
                    "Invalid status transition from " + application.getStatus() + " to " + target);
        }
        application.setStatus(target);
        application.setUpdatedAt(java.time.LocalDateTime.now());
        log.info("Application {} status changed: {} -> {}", id, application.getStatus(), target);
        return applicationMapper.toResponse(applicationRepository.save(application));
    }

    private Application getEntity(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application not found: " + id));
    }
}
