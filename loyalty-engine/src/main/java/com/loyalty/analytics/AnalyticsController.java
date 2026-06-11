package com.loyalty.analytics;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * GET /api/analytics/summary
     * Total customers, transactions, points issued, tier distribution, avg churn score.
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        return ResponseEntity.ok(analyticsService.getSummary());
    }

    /**
     * GET /api/analytics/at-risk
     * Customers with churnScore >= 0.7
     */
    @GetMapping("/at-risk")
    public ResponseEntity<List<AtRiskCustomerDTO>> getAtRiskCustomers() {
        return ResponseEntity.ok(analyticsService.getAtRiskCustomers());
    }

    /**
     * GET /api/analytics/top-customers
     * Top 10 customers by total points.
     */
    @GetMapping("/top-customers")
    public ResponseEntity<List<TopCustomerDTO>> getTopCustomers() {
        return ResponseEntity.ok(analyticsService.getTopCustomers());
    }

    /**
     * GET /api/analytics/offer-performance
     * Offers assigned vs redeemed breakdown.
     */
    @GetMapping("/offer-performance")
    public ResponseEntity<Map<String, Object>> getOfferPerformance() {
        return ResponseEntity.ok(analyticsService.getOfferPerformance());
    }
}
