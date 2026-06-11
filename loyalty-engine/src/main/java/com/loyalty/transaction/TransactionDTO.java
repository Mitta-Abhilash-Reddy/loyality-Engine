package com.loyalty.transaction;

import com.loyalty.customer.Customer;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TransactionDTO {
    private Long id;
    private Long customerId;
    private String customerName;
    private Transaction.TransactionType type;
    private Double amount;
    private Integer pointsEarned;
    private Integer pointsRedeemed;
    private String description;
    private Boolean weekendBonus;
    private LocalDateTime createdAt;
    private Customer.Tier tierAfter;

    public static TransactionDTO from(Transaction t) {
        return TransactionDTO.builder()
                .id(t.getId())
                .customerId(t.getCustomer().getId())
                .customerName(t.getCustomer().getName())
                .type(t.getType())
                .amount(t.getAmount())
                .pointsEarned(t.getPointsEarned())
                .pointsRedeemed(t.getPointsRedeemed())
                .description(t.getDescription())
                .weekendBonus(t.getWeekendBonus())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
