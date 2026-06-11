package com.loyalty.analytics;

import com.loyalty.customer.Customer;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AtRiskCustomerDTO {
    private Long id;
    private String name;
    private String email;
    private String tier;
    private int totalPoints;
    private double churnScore;
    private String riskLevel;

    public static AtRiskCustomerDTO from(Customer c) {
        String risk = c.getChurnScore() >= 0.7 ? "HIGH" : c.getChurnScore() >= 0.4 ? "MEDIUM" : "LOW";
        return AtRiskCustomerDTO.builder()
                .id(c.getId())
                .name(c.getName())
                .email(c.getEmail())
                .tier(c.getTier().name())
                .totalPoints(c.getTotalPoints())
                .churnScore(c.getChurnScore())
                .riskLevel(risk)
                .build();
    }
}
