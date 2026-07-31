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
import com.recruitment.model.Candidate;
import com.recruitment.model.Offer;
import com.recruitment.repository.ApplicationRepository;
import com.recruitment.repository.OfferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OfferServiceTest {

    @Mock
    private OfferRepository offerRepository;
    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private ApplicationService applicationService;
    @Mock
    private OfferMapper offerMapper;

    @InjectMocks
    private OfferService offerService;

    private Candidate candidate;
    private Application application;
    private Offer offer;

    @BeforeEach
    void setUp() {
        candidate = Candidate.builder().id(1L).email("candidate@test.com").firstName("Jane").lastName("Doe").build();
        application = Application.builder().id(1L).candidate(candidate).status(ApplicationStatus.OFFERED).build();
        offer = Offer.builder()
                .id(1L)
                .salary(BigDecimal.valueOf(2500000))
                .status(OfferStatus.PENDING)
                .application(application)
                .build();
    }

    @Test
    void create_shouldRequireOfferedStatus() {
        Application notOffered = Application.builder().id(1L).status(ApplicationStatus.HR_INTERVIEW).build();
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(notOffered));

        assertThatThrownBy(() -> offerService.create(1L, new OfferRequest(BigDecimal.valueOf(2500000), null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OFFERED status");
    }

    @Test
    void create_shouldSucceedWhenApplicationIsOffered() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(offerRepository.findByApplicationId(1L)).thenReturn(Optional.empty());
        when(offerRepository.save(any())).thenReturn(offer);
        when(offerMapper.toResponse(any())).thenReturn(
                new OfferResponse(1L, BigDecimal.valueOf(2500000), null, OfferStatus.PENDING, null, 1L, "Backend Engineer", "Jane Doe"));

        OfferResponse result = offerService.create(1L, new OfferRequest(BigDecimal.valueOf(2500000), null));

        assertThat(result.status()).isEqualTo(OfferStatus.PENDING);
    }

    @Test
    void create_shouldRejectDuplicateOffer() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(offerRepository.findByApplicationId(1L)).thenReturn(Optional.of(offer));

        assertThatThrownBy(() -> offerService.create(1L, new OfferRequest(BigDecimal.valueOf(2500000), null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void respond_shouldRejectNonCandidate() {
        Candidate other = Candidate.builder().id(2L).email("other@test.com").build();
        application.setCandidate(other);
        when(offerRepository.findByApplicationId(1L)).thenReturn(Optional.of(offer));

        assertThatThrownBy(() -> offerService.respond("candidate@test.com", 1L,
                new OfferResponseRequest(OfferStatus.ACCEPTED)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void respond_shouldRejectWhenAlreadyResponded() {
        offer.setStatus(OfferStatus.ACCEPTED);
        when(offerRepository.findByApplicationId(1L)).thenReturn(Optional.of(offer));

        assertThatThrownBy(() -> offerService.respond("candidate@test.com", 1L,
                new OfferResponseRequest(OfferStatus.DECLINED)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already been responded");
    }

    @Test
    void respond_accept_shouldAdvanceApplicationToAccepted() {
        when(offerRepository.findByApplicationId(1L)).thenReturn(Optional.of(offer));
        when(offerRepository.save(any())).thenReturn(offer);
        when(offerMapper.toResponse(any())).thenReturn(
                new OfferResponse(1L, BigDecimal.valueOf(2500000), null, OfferStatus.ACCEPTED, null, 1L, "Backend Engineer", "Jane Doe"));

        OfferResponse result = offerService.respond("candidate@test.com", 1L,
                new OfferResponseRequest(OfferStatus.ACCEPTED));

        assertThat(result.status()).isEqualTo(OfferStatus.ACCEPTED);
        verify(applicationService).updateStatus(eq(1L), any(ApplicationStatusRequest.class));
    }
}
