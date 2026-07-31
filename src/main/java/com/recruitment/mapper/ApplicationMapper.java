package com.recruitment.mapper;

import com.recruitment.dto.ApplicationResponse;
import com.recruitment.model.Application;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {

    @Mapping(target = "jobId", source = "application.jobPosting.id")
    @Mapping(target = "jobTitle", source = "application.jobPosting.title")
    @Mapping(target = "companyName", source = "application.jobPosting.company.name")
    @Mapping(target = "candidateId", source = "application.candidate.id")
    @Mapping(target = "candidateName", expression = "java(application.getCandidate().getFirstName() + \" \" + application.getCandidate().getLastName())")
    ApplicationResponse toResponse(Application application);
}
