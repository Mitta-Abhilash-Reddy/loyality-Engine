package com.loyalty.notification;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class NotificationDTO {

    private Long id;
    private Long customerId;
    private String customerName;
    private String type;
    private String message;
    private String status;
    private int retryCount;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;

    public static NotificationDTO from(Notification n) {
        return NotificationDTO.builder()
                .id(n.getId())
                .customerId(n.getCustomer().getId())
                .customerName(n.getCustomer().getName())
                .type(n.getType().name())
                .message(n.getMessage())
                .status(n.getStatus().name())
                .retryCount(n.getRetryCount())
                .createdAt(n.getCreatedAt())
                .sentAt(n.getSentAt())
                .build();
    }
}
