package com.loyalty.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Notification> findByCustomerIdAndStatus(Long customerId, Notification.NotificationStatus status);

    List<Notification> findByStatusAndRetryCountLessThan(Notification.NotificationStatus status, int maxRetries);
}
