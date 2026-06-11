package com.loyalty.offer;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
public class OfferController {

    private final OfferService offerService;

    /**
     * POST /api/offers/assign/{customerId}
     * Manually trigger offer assignment for a customer.
     */
    @PostMapping("/assign/{customerId}")
    public ResponseEntity<?> assignOffer(@PathVariable Long customerId) {
        OfferDTO offer = offerService.assignOffer(customerId);
        if (offer == null) {
            return ResponseEntity.ok().body(
                    java.util.Map.of("message", "No offer assigned — customer already has an active offer or churn score is below threshold")
            );
        }
        return ResponseEntity.ok(offer);
    }

    /**
     * GET /api/offers/{customerId}
     * Get all offers for a customer (all statuses).
     */
    @GetMapping("/{customerId}")
    public ResponseEntity<List<OfferDTO>> getOffers(@PathVariable Long customerId) {
        return ResponseEntity.ok(offerService.getOffers(customerId));
    }

    /**
     * GET /api/offers/{customerId}/active
     * Get only active offers for a customer.
     */
    @GetMapping("/{customerId}/active")
    public ResponseEntity<List<OfferDTO>> getActiveOffers(@PathVariable Long customerId) {
        return ResponseEntity.ok(offerService.getActiveOffers(customerId));
    }
}
