package com.loyalty.offer;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OfferRepository extends JpaRepository<Offer, Long> {

    List<Offer> findByCustomerIdAndStatus(Long customerId, Offer.OfferStatus status);

    List<Offer> findByCustomerIdOrderByAssignedAtDesc(Long customerId);

    Optional<Offer> findFirstByCustomerIdAndStatus(Long customerId, Offer.OfferStatus status);
}
