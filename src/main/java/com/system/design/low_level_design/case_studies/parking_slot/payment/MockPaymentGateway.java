package com.system.design.low_level_design.case_studies.parking_slot.payment;

import com.system.design.low_level_design.case_studies.parking_slot.enums.PaymentMethod;
import com.system.design.low_level_design.case_studies.parking_slot.interfaces.PaymentGateway;
import com.system.design.low_level_design.case_studies.parking_slot.models.Money;
import com.system.design.low_level_design.case_studies.parking_slot.models.PaymentResult;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * Mock payment gateway for demo/testing.
 * Simulates ~95% success rate; retryable transient errors for the rest.
 */
public class MockPaymentGateway implements PaymentGateway {

    private static final Logger LOG = Logger.getLogger(MockPaymentGateway.class.getName());

    @Override
    public PaymentResult charge(String ticketId, Money amount, PaymentMethod method, String idempotencyKey) {
        LOG.info("Charging " + amount + " for ticket " + ticketId + " via " + method);

        // Simulate payment attempt
        double roll = Math.random();
        if (roll < 0.93) {
            String providerRef = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            return PaymentResult.success(idempotencyKey, providerRef, amount.getAmount());
        } else if (roll < 0.97) {
            return PaymentResult.failure(idempotencyKey, "Gateway timeout", true);
        } else {
            return PaymentResult.failure(idempotencyKey, "Card declined", false);
        }
    }

    @Override
    public PaymentResult refund(String paymentId, Money amount) {
        LOG.info("Refunding " + amount + " for payment " + paymentId);
        String providerRef = "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return PaymentResult.success(paymentId, providerRef, amount.getAmount());
    }

    @Override
    public String providerName() { return "MockGateway"; }
}

