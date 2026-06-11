package com.loyalty.analytics;

import com.loyalty.customer.Customer;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopCustomerDTO {
    private Long id;
    private String name;
    private String email;
    private String tier;
    private int totalPoints;
    private double churnScore;

    public static TopCustomerDTO from(Customer c) {
        return TopCustomerDTO.builder()
                .id(c.getId())
                .name(c.getName())
                .email(c.getEmail())
                .tier(c.getTier().name())
                .totalPoints(c.getTotalPoints())
                .churnScore(c.getChurnScore())
                .build();
    }
}
