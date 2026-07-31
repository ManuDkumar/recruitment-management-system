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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobPostingService {

    private static final Map<JobStatus, Set<JobStatus>> ALLOWED_TRANSITIONS = Map.of(
            JobStatus.DRAFT, Set.of(JobStatus.OPEN),
            JobStatus.OPEN, Set.of(JobStatus.CLOSED),
            JobStatus.CLOSED, Set.of(JobStatus.OPEN)
    );

    private final JobPostingRepository jobPostingRepository;
    private final CompanyRepository companyRepository;
    private final JobPostingMapper jobPostingMapper;

    public JobPostingResponse create(JobPostingRequest request) {
        Company company = getCompany(request.companyId());

        JobPosting jobPosting = jobPostingMapper.toEntity(request);
        jobPosting.setCompany(company);
        JobPosting saved = jobPostingRepository.save(jobPosting);
        log.info("Job posting created: id={} title={} companyId={}", saved.getId(), saved.getTitle(), company.getId());
        return jobPostingMapper.toResponse(saved);
    }

    public JobPostingResponse update(Long id, JobPostingRequest request) {
        JobPosting jobPosting = getEntity(id);
        jobPostingMapper.updateEntity(request, jobPosting);

        if (!jobPosting.getCompany().getId().equals(request.companyId())) {
            jobPosting.setCompany(getCompany(request.companyId()));
        }
        jobPosting.setUpdatedAt(LocalDateTime.now());

        log.info("Job posting updated: id={} title={}", id, jobPosting.getTitle());
        return jobPostingMapper.toResponse(jobPostingRepository.save(jobPosting));
    }

    public JobPostingResponse updateStatus(Long id, JobStatus newStatus) {
        JobPosting jobPosting = getEntity(id);
        JobStatus currentStatus = jobPosting.getStatus();

        if (currentStatus != newStatus
                && !ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of()).contains(newStatus)) {
            throw new IllegalArgumentException(
                    "Invalid status transition from " + currentStatus + " to " + newStatus);
        }

        jobPosting.setStatus(newStatus);
        jobPosting.setUpdatedAt(LocalDateTime.now());

        log.info("Job posting status changed: id={} {} -> {}", id, currentStatus, newStatus);
        return jobPostingMapper.toResponse(jobPostingRepository.save(jobPosting));
    }

    public JobPostingResponse getById(Long id) {
        return jobPostingMapper.toResponse(getEntity(id));
    }

    public List<JobPostingResponse> getAll(Long companyId, JobStatus status) {
        List<JobPosting> jobs;
        if (companyId != null && status != null) {
            jobs = jobPostingRepository.findByCompanyIdAndStatus(companyId, status);
        } else if (companyId != null) {
            jobs = jobPostingRepository.findByCompanyId(companyId);
        } else if (status != null) {
            jobs = jobPostingRepository.findByStatus(status);
        } else {
            jobs = jobPostingRepository.findAll();
        }
        return jobs.stream().map(jobPostingMapper::toResponse).toList();
    }

    public void delete(Long id) {
        JobPosting jobPosting = getEntity(id);
        jobPostingRepository.delete(jobPosting);
        log.info("Job posting deleted: id={} title={}", id, jobPosting.getTitle());
    }

    private JobPosting getEntity(Long id) {
        return jobPostingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Job posting not found with id: " + id));
    }

    private Company getCompany(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Company not found with id: " + id));
    }
}
