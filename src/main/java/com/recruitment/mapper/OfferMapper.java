package com.recruitment.mapper;

import com.recruitment.dto.OfferResponse;
import com.recruitment.model.Offer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OfferMapper {

    @Mapping(target = "applicationId", source = "offer.application.id")
    @Mapping(target = "jobTitle", source = "offer.application.jobPosting.title")
    @Mapping(target = "candidateName", expression = "java(offer.getApplication().getCandidate().getFirstName() + \" \" + offer.getApplication().getCandidate().getLastName())")
    OfferResponse toResponse(Offer offer);
}
