package com.system.design.low_level_design.lld_case_studies.parking_slot.interfaces;

import com.system.design.low_level_design.lld_case_studies.parking_slot.enums.PaymentMethod;
import com.system.design.low_level_design.lld_case_studies.parking_slot.models.Money;
import com.system.design.low_level_design.lld_case_studies.parking_slot.models.PaymentResult;

/**
 * Abstraction over external payment providers (card, UPI, wallet, etc.).
 */
public interface PaymentGateway {

    /**
     * Captures a payment.
     *
     * @param ticketId       the ticket being paid for
     * @param amount         amount to charge
     * @param method         payment method
     * @param idempotencyKey unique key to prevent duplicate charges
     * @return result of the capture attempt
     */
    PaymentResult charge(String ticketId, Money amount, PaymentMethod method, String idempotencyKey);

    /**
     * Refunds a previous successful payment.
     *
     * @param paymentId    original payment ID
     * @param amount       amount to refund
     * @return result of the refund attempt
     */
    PaymentResult refund(String paymentId, Money amount);

    /**
     * Returns the provider name (e.g., "Razorpay", "Stripe").
     */
    String providerName();
}

