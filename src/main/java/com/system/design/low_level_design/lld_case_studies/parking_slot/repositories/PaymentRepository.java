package com.system.design.low_level_design.lld_case_studies.parking_slot.repositories;

import com.system.design.low_level_design.lld_case_studies.parking_slot.entities.Payment;

import java.util.Optional;

/**
 * Repository for Payment persistence.
 */
public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(String paymentId);

    /**
     * Finds by idempotency key to prevent duplicate charges.
     */
    Optional<Payment> findByIdempotencyKey(String key);

    Optional<Payment> findByTicketId(String ticketId);

    Payment update(Payment payment);
}

