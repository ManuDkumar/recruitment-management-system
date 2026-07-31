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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private JobPostingRepository jobPostingRepository;
    @Mock
    private CandidateRepository candidateRepository;
    @Mock
    private CandidateService candidateService;
    @Mock
    private ApplicationMapper applicationMapper;

    @InjectMocks
    private ApplicationService applicationService;

    private Candidate candidate;
    private JobPosting openJob;
    private Application application;

    @BeforeEach
    void setUp() {
        candidate = Candidate.builder()
                .id(1L)
                .email("candidate@test.com")
                .firstName("Jane")
                .lastName("Doe")
                .build();
        openJob = JobPosting.builder().id(1L).title("Backend Engineer").status(JobStatus.OPEN).build();
        application = Application.builder()
                .id(1L)
                .candidate(candidate)
                .jobPosting(openJob)
                .status(ApplicationStatus.APPLIED)
                .build();
    }

    @Test
    void apply_shouldCreateApplicationWithAppliedStatus() {
        when(candidateService.getOrCreateEntity("candidate@test.com")).thenReturn(candidate);
        when(jobPostingRepository.findById(1L)).thenReturn(Optional.of(openJob));
        when(applicationRepository.existsByCandidateIdAndJobPostingId(1L, 1L)).thenReturn(false);
        when(applicationRepository.save(any())).thenReturn(application);
        when(applicationMapper.toResponse(any())).thenReturn(
                new ApplicationResponse(1L, ApplicationStatus.APPLIED, null, null, 1L, "Backend Engineer", "Acme Corp", 1L, "Jane Doe"));

        ApplicationResponse result = applicationService.apply("candidate@test.com", new ApplicationRequest(1L));

        assertThat(result.status()).isEqualTo(ApplicationStatus.APPLIED);
        verify(applicationRepository).save(any());
    }

    @Test
    void apply_shouldRejectClosedJob() {
        JobPosting draftJob = JobPosting.builder().id(2L).title("Internal Role").status(JobStatus.DRAFT).build();
        when(candidateService.getOrCreateEntity("candidate@test.com")).thenReturn(candidate);
        when(jobPostingRepository.findById(2L)).thenReturn(Optional.of(draftJob));

        assertThatThrownBy(() -> applicationService.apply("candidate@test.com", new ApplicationRequest(2L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not open for applications");
    }

    @Test
    void apply_shouldRejectDuplicate() {
        when(candidateService.getOrCreateEntity("candidate@test.com")).thenReturn(candidate);
        when(jobPostingRepository.findById(1L)).thenReturn(Optional.of(openJob));
        when(applicationRepository.existsByCandidateIdAndJobPostingId(1L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> applicationService.apply("candidate@test.com", new ApplicationRequest(1L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already applied");
    }

    @Test
    void apply_shouldThrowWhenJobMissing() {
        when(candidateService.getOrCreateEntity("candidate@test.com")).thenReturn(candidate);
        when(jobPostingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.apply("candidate@test.com", new ApplicationRequest(99L)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateStatus_shouldRejectInvalidTransition() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> applicationService.updateStatus(1L, new ApplicationStatusRequest(ApplicationStatus.OFFERED)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @Test
    void updateStatus_shouldApplyValidTransition() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(applicationRepository.save(any())).thenReturn(application);
        when(applicationMapper.toResponse(any())).thenReturn(
                new ApplicationResponse(1L, ApplicationStatus.SCREENING, null, null, 1L, "Backend Engineer", "Acme Corp", 1L, "Jane Doe"));

        applicationService.updateStatus(1L, new ApplicationStatusRequest(ApplicationStatus.SCREENING));

        assertThat(application.getStatus()).isEqualTo(ApplicationStatus.SCREENING);
        verify(applicationRepository).save(application);
    }
}
