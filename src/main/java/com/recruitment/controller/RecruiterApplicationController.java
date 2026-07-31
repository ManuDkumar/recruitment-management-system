package com.recruitment.controller;

import com.recruitment.dto.ApplicationResponse;
import com.recruitment.dto.ApplicationStatusRequest;
import com.recruitment.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recruiter")
@RequiredArgsConstructor
public class RecruiterApplicationController {

    private final ApplicationService applicationService;

    @GetMapping("/jobs/{jobId}/applications")
    public List<ApplicationResponse> listByJob(@PathVariable Long jobId) {
        return applicationService.getByJob(jobId);
    }

    @PatchMapping("/applications/{id}/status")
    public ApplicationResponse updateStatus(@PathVariable Long id,
                                            @Valid @RequestBody ApplicationStatusRequest request) {
        return applicationService.updateStatus(id, request);
    }
}
