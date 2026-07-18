package com.system.design.low_level_design.lld_case_studies.parking_slot.service;

import com.system.design.low_level_design.lld_case_studies.parking_slot.entities.Payment;
import com.system.design.low_level_design.lld_case_studies.parking_slot.enums.PaymentMethod;
import com.system.design.low_level_design.lld_case_studies.parking_slot.exceptions.PaymentException;
import com.system.design.low_level_design.lld_case_studies.parking_slot.interfaces.PaymentGateway;
import com.system.design.low_level_design.lld_case_studies.parking_slot.models.Money;
import com.system.design.low_level_design.lld_case_studies.parking_slot.models.PaymentResult;
import com.system.design.low_level_design.lld_case_studies.parking_slot.repositories.PaymentRepository;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * Orchestrates payment capture and refund with idempotency protection.
 */
public class PaymentService {

    private static final Logger LOG = Logger.getLogger(PaymentService.class.getName());

    private final PaymentRepository paymentRepository;
    private final PaymentGateway gateway;

    public PaymentService(PaymentRepository paymentRepository, PaymentGateway gateway) {
        this.paymentRepository = paymentRepository;
        this.gateway = gateway;
    }

    /**
     * Charges payment for a ticket. Idempotent by {@code idempotencyKey}.
     *
     * @return persisted Payment entity
     * @throws PaymentException on non-retryable failure
     */
    public Payment charge(String ticketId, Money amount, PaymentMethod method, String idempotencyKey) {
        // Idempotency: return existing payment if already processed
        Optional<Payment> existing = paymentRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            LOG.info("Returning cached payment for idempotency key: " + idempotencyKey);
            return existing.get();
        }

        Payment payment = new Payment(ticketId, amount.getAmount(), method, idempotencyKey);
        paymentRepository.save(payment);

        PaymentResult result = gateway.charge(ticketId, amount, method, idempotencyKey);

        if (result.isSuccess()) {
            payment.markSuccess(result.getProviderRef());
        } else {
            payment.markFailed(result.getErrorMessage());
            paymentRepository.update(payment);
            throw new PaymentException("Payment failed: " + result.getErrorMessage(), result.isRetryable());
        }

        paymentRepository.update(payment);
        LOG.info("Payment succeeded: " + payment.getPaymentId() + " ref=" + result.getProviderRef());
        return payment;
    }

    /**
     * Issues a refund for a previously successful payment.
     */
    public Payment refund(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found: " + paymentId, false));

        PaymentResult result = gateway.refund(paymentId, new Money(payment.getAmount()));

        if (result.isSuccess()) {
            payment.markRefunded();
            paymentRepository.update(payment);
        } else {
            throw new PaymentException("Refund failed for payment: " + paymentId, true);
        }

        return payment;
    }
}

