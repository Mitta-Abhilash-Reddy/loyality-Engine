package com.loyalty.customer;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CustomerDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String tier;
    private Integer totalPoints;
    private Double churnScore;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime lastTransactionAt;

    public static CustomerDTO from(Customer c) {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setEmail(c.getEmail());
        dto.setPhone(c.getPhone());
        dto.setTier(c.getTier().name());
        dto.setTotalPoints(c.getTotalPoints());
        dto.setChurnScore(c.getChurnScore());
        dto.setActive(c.isActive());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setLastTransactionAt(c.getLastTransactionAt());
        return dto;
    }
}
