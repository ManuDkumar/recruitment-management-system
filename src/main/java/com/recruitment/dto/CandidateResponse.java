package com.recruitment.dto;

public record CandidateResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String resumePath
) {}
