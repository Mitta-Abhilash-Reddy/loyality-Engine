package com.loyalty.transaction;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RedeemRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Points to redeem is required")
    @Min(value = 1, message = "Points must be at least 1")
    private Integer points;

    private String description;
}
