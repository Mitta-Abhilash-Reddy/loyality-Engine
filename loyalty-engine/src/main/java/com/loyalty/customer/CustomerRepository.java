package com.loyalty.customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    Page<Customer> findByActiveTrue(Pageable pageable);

    Optional<Customer> findByIdAndActiveTrue(Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Customer c SET c.churnScore = :churnScore WHERE c.id = :customerId")
    void updateChurnScore(@Param("customerId") Long customerId, @Param("churnScore") double churnScore);
}