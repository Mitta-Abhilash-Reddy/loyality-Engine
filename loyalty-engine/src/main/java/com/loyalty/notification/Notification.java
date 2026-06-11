package com.loyalty.notification;

import com.loyalty.customer.Customer;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    public enum NotificationType { OFFER_ASSIGNED, POINTS_EARNED, POINTS_REDEEMED, TIER_UPGRADE, CHURN_ALERT }
    public enum NotificationStatus { PENDING, SENT, FAILED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private NotificationType type = NotificationType.OFFER_ASSIGNED;

    private String message;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;

    @Builder.Default
    private int retryCount = 0;

    private LocalDateTime createdAt;

    private LocalDateTime sentAt;
}
