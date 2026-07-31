package com.recruitment.dto;

import jakarta.validation.constraints.NotBlank;

public record CompanyRequest(
        @NotBlank String name,
        String description,
        String location,
        String website
) {}
