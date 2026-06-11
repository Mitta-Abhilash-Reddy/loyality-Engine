package com.loyalty.analytics;

import com.loyalty.customer.Customer;
import com.loyalty.offer.Offer;
import com.loyalty.offer.OfferRepository;
import com.loyalty.transaction.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;
    private final TransactionRepository transactionRepository;
    private final OfferRepository offerRepository;

    public Map<String, Object> getSummary() {
        long totalCustomers = analyticsRepository.countByActiveTrue();
        long totalTransactions = transactionRepository.count();
        long totalPointsIssued = analyticsRepository.sumTotalPoints();
        double avgChurnScore = analyticsRepository.avgChurnScore();

        // Tier distribution
        Map<String, Long> tierDistribution = new LinkedHashMap<>();
        for (Customer.Tier tier : Customer.Tier.values()) {
            tierDistribution.put(tier.name(), analyticsRepository.countByTier(tier));
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalActiveCustomers", totalCustomers);
        summary.put("totalTransactions", totalTransactions);
        summary.put("totalPointsIssued", totalPointsIssued);
        summary.put("averageChurnScore", Math.round(avgChurnScore * 1000.0) / 1000.0);
        summary.put("tierDistribution", tierDistribution);

        return summary;
    }

    public List<AtRiskCustomerDTO> getAtRiskCustomers() {
        return analyticsRepository.findAtRiskCustomers()
                .stream()
                .map(AtRiskCustomerDTO::from)
                .collect(Collectors.toList());
    }

    public List<TopCustomerDTO> getTopCustomers() {
        return analyticsRepository.findTop10ByPoints(PageRequest.of(0, 10))
                .stream()
                .map(TopCustomerDTO::from)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getOfferPerformance() {
        List<Offer> allOffers = offerRepository.findAll();

        long totalAssigned = allOffers.size();
        long active = allOffers.stream().filter(o -> o.getStatus() == Offer.OfferStatus.ACTIVE).count();
        long redeemed = allOffers.stream().filter(o -> o.getStatus() == Offer.OfferStatus.REDEEMED).count();
        long expired = allOffers.stream().filter(o -> o.getStatus() == Offer.OfferStatus.EXPIRED).count();

        // Breakdown by offer type
        Map<String, Long> byType = allOffers.stream()
                .collect(Collectors.groupingBy(o -> o.getOfferType() != null ? o.getOfferType().name() : "UNKNOWN",
                        Collectors.counting()));

        double redemptionRate = totalAssigned > 0
                ? Math.round((redeemed * 100.0 / totalAssigned) * 10.0) / 10.0
                : 0.0;

        Map<String, Object> performance = new LinkedHashMap<>();
        performance.put("totalAssigned", totalAssigned);
        performance.put("active", active);
        performance.put("redeemed", redeemed);
        performance.put("expired", expired);
        performance.put("redemptionRatePercent", redemptionRate);
        performance.put("byOfferType", byType);

        return performance;
    }
}
