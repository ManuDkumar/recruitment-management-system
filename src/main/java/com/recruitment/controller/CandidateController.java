package com.recruitment.controller;

import com.recruitment.dto.CandidateRequest;
import com.recruitment.dto.CandidateResponse;
import com.recruitment.service.CandidateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/me/candidate")
@RequiredArgsConstructor
public class CandidateController {

    private final CandidateService candidateService;

    @GetMapping
    public CandidateResponse getProfile(Authentication authentication) {
        return candidateService.getOrCreate(authentication.getName());
    }

    @PutMapping
    public CandidateResponse updateProfile(Authentication authentication, @Valid @RequestBody CandidateRequest request) {
        return candidateService.update(authentication.getName(), request);
    }

    @PostMapping(value = "/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CandidateResponse uploadResume(Authentication authentication, @RequestParam("file") MultipartFile file) {
        return candidateService.uploadResume(authentication.getName(), file);
    }

    @GetMapping("/resume")
    public ResponseEntity<Resource> downloadResume(Authentication authentication) {
        CandidateService.ResumeFile resumeFile = candidateService.loadResume(authentication.getName());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resumeFile.filename() + "\"")
                .body(resumeFile.resource());
    }
}
