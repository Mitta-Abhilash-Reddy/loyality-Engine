package com.loyalty.analytics;

import com.loyalty.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnalyticsRepository extends JpaRepository<Customer, Long> {

    // Total active customers
    long countByActiveTrue();

    // Total points issued across all customers
    @Query("SELECT COALESCE(SUM(c.totalPoints), 0) FROM Customer c WHERE c.active = true")
    long sumTotalPoints();

    // Average churn score
    @Query("SELECT COALESCE(AVG(c.churnScore), 0.0) FROM Customer c WHERE c.active = true")
    double avgChurnScore();

    // Count by tier
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.tier = :tier AND c.active = true")
    long countByTier(@Param("tier") Customer.Tier tier);

    // At-risk customers (churnScore >= 0.7)
    @Query("SELECT c FROM Customer c WHERE c.churnScore >= 0.7 AND c.active = true ORDER BY c.churnScore DESC")
    List<Customer> findAtRiskCustomers();

    // Top 10 by points
    @Query("SELECT c FROM Customer c WHERE c.active = true ORDER BY c.totalPoints DESC")
    List<Customer> findTop10ByPoints(org.springframework.data.domain.Pageable pageable);
}
