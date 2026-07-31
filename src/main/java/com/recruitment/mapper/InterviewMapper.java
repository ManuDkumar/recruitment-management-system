package com.recruitment.mapper;

import com.recruitment.dto.InterviewResponse;
import com.recruitment.model.Interview;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InterviewMapper {

    @Mapping(target = "applicationId", source = "interview.application.id")
    @Mapping(target = "interviewerId", source = "interview.interviewer.id")
    @Mapping(target = "interviewerName", source = "interview.interviewer.name")
    InterviewResponse toResponse(Interview interview);
}
