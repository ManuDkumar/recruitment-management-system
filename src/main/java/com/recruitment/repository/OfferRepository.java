package com.recruitment.repository;

import com.recruitment.enums.OfferStatus;
import com.recruitment.model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OfferRepository extends JpaRepository<Offer, Long> {
    Optional<Offer> findByApplicationId(Long applicationId);
    List<Offer> findByStatus(OfferStatus status);
    long countByStatus(OfferStatus status);
}
