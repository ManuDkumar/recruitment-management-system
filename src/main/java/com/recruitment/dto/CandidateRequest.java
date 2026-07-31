package com.recruitment.dto;

import jakarta.validation.constraints.NotBlank;

public record CandidateRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        String phone
) {}
