package com.loyalty.offer;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class OfferDTO {

    private Long id;
    private Long customerId;
    private String customerName;
    private String title;
    private String description;
    private String offerType;
    private String status;
    private LocalDateTime assignedAt;
    private LocalDateTime expiresAt;

    public static OfferDTO from(Offer offer) {
        return OfferDTO.builder()
                .id(offer.getId())
                .customerId(offer.getCustomer().getId())
                .customerName(offer.getCustomer().getName())
                .title(offer.getTitle())
                .description(offer.getDescription())
                .offerType(offer.getOfferType().name())
                .status(offer.getStatus().name())
                .assignedAt(offer.getAssignedAt())
                .expiresAt(offer.getExpiresAt())
                .build();
    }
}
