package com.recruitment.service;

import com.recruitment.dto.JobPostingRequest;
import com.recruitment.dto.JobPostingResponse;
import com.recruitment.enums.JobStatus;
import com.recruitment.exception.NotFoundException;
import com.recruitment.mapper.JobPostingMapper;
import com.recruitment.model.Company;
import com.recruitment.model.JobPosting;
import com.recruitment.repository.CompanyRepository;
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
class JobPostingServiceTest {

    @Mock
    private JobPostingRepository jobPostingRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private JobPostingMapper jobPostingMapper;

    @InjectMocks
    private JobPostingService jobPostingService;

    private Company company;
    private JobPosting jobPosting;

    @BeforeEach
    void setUp() {
        company = Company.builder().id(1L).name("Acme Corp").build();
        jobPosting = JobPosting.builder()
                .id(1L)
                .title("Backend Engineer")
                .status(JobStatus.DRAFT)
                .company(company)
                .build();
    }

    @Test
    void create_shouldSaveNewJobPosting() {
        JobPostingRequest request = new JobPostingRequest("Backend Engineer", null, null, null, 1L);
        when(companyRepository.findById(1L)).thenReturn(Optional.of(company));
        when(jobPostingMapper.toEntity(request)).thenReturn(jobPosting);
        when(jobPostingRepository.save(any())).thenReturn(jobPosting);
        when(jobPostingMapper.toResponse(any())).thenReturn(
                new JobPostingResponse(1L, "Backend Engineer", null, null, null, JobStatus.DRAFT, 1L, "Acme Corp", null, null));

        JobPostingResponse result = jobPostingService.create(request);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(jobPosting.getCompany()).isEqualTo(company);
        verify(jobPostingRepository).save(any());
    }

    @Test
    void create_shouldThrowWhenCompanyMissing() {
        JobPostingRequest request = new JobPostingRequest("Backend Engineer", null, null, null, 99L);
        when(companyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobPostingService.create(request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateStatus_shouldAllowDraftToOpen() {
        when(jobPostingRepository.findById(1L)).thenReturn(Optional.of(jobPosting));
        when(jobPostingMapper.toResponse(any())).thenReturn(
                new JobPostingResponse(1L, "Backend Engineer", null, null, null, JobStatus.OPEN, 1L, "Acme Corp", null, null));

        jobPostingService.updateStatus(1L, JobStatus.OPEN);

        assertThat(jobPosting.getStatus()).isEqualTo(JobStatus.OPEN);
        verify(jobPostingRepository).save(jobPosting);
    }

    @Test
    void updateStatus_shouldRejectOpenToDraft() {
        jobPosting.setStatus(JobStatus.OPEN);
        when(jobPostingRepository.findById(1L)).thenReturn(Optional.of(jobPosting));

        assertThatThrownBy(() -> jobPostingService.updateStatus(1L, JobStatus.DRAFT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @Test
    void updateStatus_shouldAllowClosedToOpen() {
        jobPosting.setStatus(JobStatus.CLOSED);
        when(jobPostingRepository.findById(1L)).thenReturn(Optional.of(jobPosting));
        when(jobPostingMapper.toResponse(any())).thenReturn(
                new JobPostingResponse(1L, "Backend Engineer", null, null, null, JobStatus.OPEN, 1L, "Acme Corp", null, null));

        jobPostingService.updateStatus(1L, JobStatus.OPEN);

        assertThat(jobPosting.getStatus()).isEqualTo(JobStatus.OPEN);
    }

    @Test
    void getById_shouldThrowWhenMissing() {
        when(jobPostingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobPostingService.getById(1L))
                .isInstanceOf(NotFoundException.class);
    }
}
