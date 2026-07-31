package com.recruitment.service;

import com.recruitment.dto.ApplicationStatusRequest;
import com.recruitment.dto.OfferRequest;
import com.recruitment.dto.OfferResponse;
import com.recruitment.dto.OfferResponseRequest;
import com.recruitment.enums.ApplicationStatus;
import com.recruitment.enums.OfferStatus;
import com.recruitment.exception.NotFoundException;
import com.recruitment.mapper.OfferMapper;
import com.recruitment.model.Application;
import com.recruitment.model.Offer;
import com.recruitment.repository.ApplicationRepository;
import com.recruitment.repository.OfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OfferService {

    private final OfferRepository offerRepository;
    private final ApplicationRepository applicationRepository;
    private final ApplicationService applicationService;
    private final OfferMapper offerMapper;

    @Transactional
    public OfferResponse create(Long applicationId, OfferRequest request) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Application not found: " + applicationId));
        if (application.getStatus() != ApplicationStatus.OFFERED) {
            throw new IllegalArgumentException("An offer can only be created for an application at OFFERED status");
        }
        if (offerRepository.findByApplicationId(applicationId).isPresent()) {
            throw new IllegalArgumentException("An offer already exists for this application");
        }

        Offer offer = Offer.builder()
                .salary(request.salary())
                .joiningDate(request.joiningDate())
                .status(OfferStatus.PENDING)
                .application(application)
                .build();
        Offer saved = offerRepository.save(offer);
        log.info("Offer created: id={} applicationId={} salary={}", saved.getId(), applicationId, request.salary());
        return offerMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OfferResponse getByApplication(Long applicationId) {
        return offerMapper.toResponse(getEntityByApplication(applicationId));
    }

    @Transactional(readOnly = true)
    public OfferResponse getByApplicationForCandidate(String email, Long applicationId) {
        Offer offer = getEntityByApplication(applicationId);
        if (!offer.getApplication().getCandidate().getEmail().equals(email)) {
            throw new NotFoundException("Offer not found for application: " + applicationId);
        }
        return offerMapper.toResponse(offer);
    }

    @Transactional
    public OfferResponse respond(String email, Long applicationId, OfferResponseRequest request) {
        Offer offer = getEntityByApplication(applicationId);
        if (!offer.getApplication().getCandidate().getEmail().equals(email)) {
            throw new NotFoundException("Offer not found for application: " + applicationId);
        }
        if (offer.getStatus() != OfferStatus.PENDING) {
            throw new IllegalArgumentException("Offer has already been responded to");
        }
        if (request.status() != OfferStatus.ACCEPTED && request.status() != OfferStatus.DECLINED) {
            throw new IllegalArgumentException("Candidate can only ACCEPT or DECLINE an offer");
        }

        offer.setStatus(request.status());
        OfferResponse response = offerMapper.toResponse(offerRepository.save(offer));
        log.info("Offer {} {} by {}", offer.getId(), request.status(), email);

        ApplicationStatus next = request.status() == OfferStatus.ACCEPTED
                ? ApplicationStatus.ACCEPTED
                : ApplicationStatus.REJECTED;
        applicationService.updateStatus(offer.getApplication().getId(), new ApplicationStatusRequest(next));
        return response;
    }

    @Transactional(readOnly = true)
    public List<OfferResponse> list(OfferStatus status) {
        return (status == null ? offerRepository.findAll() : offerRepository.findByStatus(status)).stream()
                .map(offerMapper::toResponse)
                .toList();
    }

    private Offer getEntityByApplication(Long applicationId) {
        return offerRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new NotFoundException("Offer not found for application: " + applicationId));
    }
}
