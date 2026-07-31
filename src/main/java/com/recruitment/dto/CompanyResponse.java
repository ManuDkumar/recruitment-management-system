package com.recruitment.dto;

public record CompanyResponse(
        Long id,
        String name,
        String description,
        String location,
        String website,
        Long createdById,
        String createdByName
) {}
