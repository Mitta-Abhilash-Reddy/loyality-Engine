package com.loyalty.notification;

import com.loyalty.customer.Customer;
import com.loyalty.customer.CustomerNotFoundException;
import com.loyalty.customer.CustomerRepository;
import com.loyalty.offer.Offer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private static final int MAX_RETRIES = 3;

    private final NotificationRepository notificationRepository;
    private final CustomerRepository customerRepository;

    /**
     * Async — triggered after offer assignment.
     * Creates a PENDING notification then attempts to send it with retry logic.
     */
    @Async
    @Transactional
    public void sendOfferNotificationAsync(Long customerId, Offer offer) {
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer == null) {
            log.warn("Cannot send notification — customer {} not found", customerId);
            return;
        }

        String message = buildOfferMessage(customer, offer);

        Notification notification = Notification.builder()
                .customer(customer)
                .type(Notification.NotificationType.OFFER_ASSIGNED)
                .message(message)
                .status(Notification.NotificationStatus.PENDING)
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Notification created (id={}) for customer {}", saved.getId(), customerId);

        attemptSend(saved);
    }

    /**
     * Core send logic with up to MAX_RETRIES attempts.
     */
    private void attemptSend(Notification notification) {
        int attempts = 0;

        while (attempts < MAX_RETRIES) {
            try {
                attempts++;
                // Simulate sending (no real email/SMS needed)
                simulateSend(notification, attempts);

                notification.setStatus(Notification.NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
                notification.setRetryCount(attempts);
                notificationRepository.save(notification);

                log.info("Notification {} sent successfully on attempt {}", notification.getId(), attempts);
                return;

            } catch (Exception e) {
                log.warn("Notification {} send attempt {} failed: {}", notification.getId(), attempts, e.getMessage());

                if (attempts >= MAX_RETRIES) {
                    notification.setStatus(Notification.NotificationStatus.FAILED);
                    notification.setRetryCount(attempts);
                    notificationRepository.save(notification);
                    log.error("Notification {} permanently failed after {} attempts", notification.getId(), MAX_RETRIES);
                } else {
                    // Brief pause before retry
                    try { Thread.sleep(300L * attempts); } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }
    }

    /**
     * Simulates sending — logs the notification as if dispatched.
     * In production this would call an email/SMS gateway.
     * Throws occasionally to demonstrate retry logic (remove in prod).
     */
    private void simulateSend(Notification notification, int attempt) {
        // Simulate occasional failure on first attempt to demo retry (10% chance)
        if (attempt == 1 && Math.random() < 0.1) {
            throw new RuntimeException("Simulated send failure");
        }
        log.info("[NOTIFICATION SEND] To: {} | Type: {} | Message: {}",
                notification.getCustomer().getEmail(),
                notification.getType(),
                notification.getMessage());
    }

    private String buildOfferMessage(Customer customer, Offer offer) {
        return String.format("Hi %s! You have a new offer: %s — %s (Valid until %s)",
                customer.getName(),
                offer.getTitle(),
                offer.getDescription(),
                offer.getExpiresAt().toLocalDate());
    }

    /**
     * Get full notification history for a customer.
     */
    public List<NotificationDTO> getNotifications(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException("Customer not found: " + customerId);
        }
        return notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(NotificationDTO::from)
                .collect(Collectors.toList());
    }

    /**
     * Retry all FAILED notifications (manual trigger via API).
     */
    @Transactional
    public int retryFailed() {
        List<Notification> failed = notificationRepository
                .findByStatusAndRetryCountLessThan(Notification.NotificationStatus.FAILED, MAX_RETRIES);

        for (Notification n : failed) {
            n.setStatus(Notification.NotificationStatus.PENDING);
            notificationRepository.save(n);
            attemptSend(n);
        }

        log.info("Retried {} failed notifications", failed.size());
        return failed.size();
    }
}
