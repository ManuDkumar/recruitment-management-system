package com.recruitment.controller;

import com.recruitment.dto.JobPostingRequest;
import com.recruitment.dto.JobPostingResponse;
import com.recruitment.dto.JobStatusRequest;
import com.recruitment.enums.JobStatus;
import com.recruitment.service.JobPostingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recruiter/jobs")
@RequiredArgsConstructor
public class JobPostingController {

    private final JobPostingService jobPostingService;

    @PostMapping
    public ResponseEntity<JobPostingResponse> create(@Valid @RequestBody JobPostingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(jobPostingService.create(request));
    }

    @GetMapping
    public List<JobPostingResponse> getAll(@RequestParam(required = false) Long companyId,
                                           @RequestParam(required = false) JobStatus status) {
        return jobPostingService.getAll(companyId, status);
    }

    @GetMapping("/{id}")
    public JobPostingResponse getById(@PathVariable Long id) {
        return jobPostingService.getById(id);
    }

    @PutMapping("/{id}")
    public JobPostingResponse update(@PathVariable Long id, @Valid @RequestBody JobPostingRequest request) {
        return jobPostingService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public JobPostingResponse updateStatus(@PathVariable Long id, @Valid @RequestBody JobStatusRequest request) {
        return jobPostingService.updateStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        jobPostingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
