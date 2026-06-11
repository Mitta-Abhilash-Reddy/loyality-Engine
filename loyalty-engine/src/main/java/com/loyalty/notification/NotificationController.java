package com.loyalty.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * GET /api/notifications/{customerId}
     * Full notification history for a customer.
     */
    @GetMapping("/{customerId}")
    public ResponseEntity<List<NotificationDTO>> getNotifications(@PathVariable Long customerId) {
        return ResponseEntity.ok(notificationService.getNotifications(customerId));
    }

    /**
     * POST /api/notifications/retry-failed
     * Manually trigger retry for all FAILED notifications.
     */
    @PostMapping("/retry-failed")
    public ResponseEntity<Map<String, Object>> retryFailed() {
        int count = notificationService.retryFailed();
        return ResponseEntity.ok(Map.of(
                "message", "Retry triggered",
                "notificationsRetried", count
        ));
    }
}
