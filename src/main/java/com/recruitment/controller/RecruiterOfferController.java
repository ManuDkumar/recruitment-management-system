package com.recruitment.controller;

import com.recruitment.dto.OfferRequest;
import com.recruitment.dto.OfferResponse;
import com.recruitment.enums.OfferStatus;
import com.recruitment.service.OfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recruiter")
@RequiredArgsConstructor
public class RecruiterOfferController {

    private final OfferService offerService;

    @PostMapping("/applications/{applicationId}/offer")
    public OfferResponse create(@PathVariable Long applicationId, @Valid @RequestBody OfferRequest request) {
        return offerService.create(applicationId, request);
    }

    @GetMapping("/applications/{applicationId}/offer")
    public OfferResponse get(@PathVariable Long applicationId) {
        return offerService.getByApplication(applicationId);
    }

    @GetMapping("/offers")
    public List<OfferResponse> list(@RequestParam(required = false) OfferStatus status) {
        return offerService.list(status);
    }
}
