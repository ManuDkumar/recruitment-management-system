package com.recruitment.mapper;

import com.recruitment.dto.CandidateRequest;
import com.recruitment.dto.CandidateResponse;
import com.recruitment.model.Candidate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CandidateMapper {
    CandidateResponse toResponse(Candidate candidate);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "resumePath", ignore = true)
    @Mapping(target = "applications", ignore = true)
    void updateEntity(CandidateRequest request, @MappingTarget Candidate candidate);
}
