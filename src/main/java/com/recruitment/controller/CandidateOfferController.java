package com.recruitment.controller;

import com.recruitment.dto.OfferResponse;
import com.recruitment.dto.OfferResponseRequest;
import com.recruitment.service.OfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me/applications/{applicationId}/offer")
@RequiredArgsConstructor
public class CandidateOfferController {

    private final OfferService offerService;

    @GetMapping
    public OfferResponse get(Authentication authentication, @PathVariable Long applicationId) {
        return offerService.getByApplicationForCandidate(authentication.getName(), applicationId);
    }

    @PostMapping("/response")
    public OfferResponse respond(Authentication authentication,
                                 @PathVariable Long applicationId,
                                 @Valid @RequestBody OfferResponseRequest request) {
        return offerService.respond(authentication.getName(), applicationId, request);
    }
}
