package com.loyalty.offer;

import com.loyalty.customer.Customer;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "offers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Offer {

    public enum OfferStatus { ACTIVE, REDEEMED, EXPIRED }
    public enum OfferType { DOUBLE_POINTS, BONUS_POINTS, LOUNGE_ACCESS, GENERIC }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OfferType offerType = OfferType.GENERIC;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OfferStatus status = OfferStatus.ACTIVE;

    private LocalDateTime assignedAt;

    private LocalDateTime expiresAt;
}
