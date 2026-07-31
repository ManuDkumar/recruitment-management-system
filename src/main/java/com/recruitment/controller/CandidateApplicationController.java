package com.recruitment.controller;

import com.recruitment.dto.ApplicationRequest;
import com.recruitment.dto.ApplicationResponse;
import com.recruitment.enums.ApplicationStatus;
import com.recruitment.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/me/applications")
@RequiredArgsConstructor
public class CandidateApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    public ApplicationResponse apply(Authentication authentication, @Valid @RequestBody ApplicationRequest request) {
        return applicationService.apply(authentication.getName(), request);
    }

    @GetMapping
    public List<ApplicationResponse> list(Authentication authentication,
                                          @RequestParam(required = false) ApplicationStatus status) {
        return applicationService.getMyApplications(authentication.getName(), status);
    }

    @GetMapping("/{id}")
    public ApplicationResponse get(Authentication authentication, @PathVariable Long id) {
        return applicationService.getMyApplication(authentication.getName(), id);
    }
}
