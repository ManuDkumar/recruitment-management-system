package com.recruitment.mapper;

import com.recruitment.dto.FeedbackResponse;
import com.recruitment.model.Feedback;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {

    @Mapping(target = "interviewId", source = "feedback.interview.id")
    @Mapping(target = "userId", source = "feedback.user.id")
    @Mapping(target = "userName", source = "feedback.user.name")
    FeedbackResponse toResponse(Feedback feedback);
}
