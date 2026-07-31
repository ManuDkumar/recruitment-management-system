package com.recruitment.mapper;

import com.recruitment.dto.JobPostingRequest;
import com.recruitment.dto.JobPostingResponse;
import com.recruitment.model.JobPosting;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JobPostingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "applications", ignore = true)
    JobPosting toEntity(JobPostingRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "applications", ignore = true)
    void updateEntity(JobPostingRequest request, @MappingTarget JobPosting jobPosting);

    @Mapping(target = "companyId", source = "company.id")
    @Mapping(target = "companyName", source = "company.name")
    JobPostingResponse toResponse(JobPosting jobPosting);
}
