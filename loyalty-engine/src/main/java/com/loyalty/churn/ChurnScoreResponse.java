package com.loyalty.churn;

import lombok.Data;
import java.util.List;

@Data
public class ChurnScoreResponse {

    private Long customerId;
    private Double churnScore;
    private String riskLevel;
    private List<String> factors;
}
