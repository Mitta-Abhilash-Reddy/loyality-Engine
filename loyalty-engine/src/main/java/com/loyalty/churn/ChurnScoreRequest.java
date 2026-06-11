package com.loyalty.churn;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChurnScoreRequest {

    private Long customerId;
    private int daysSinceLastTransaction;
    private int totalPoints;
    private String tier;

    @JsonProperty("activeLastSevenDays")
    private boolean activeLastSevenDays;
}
