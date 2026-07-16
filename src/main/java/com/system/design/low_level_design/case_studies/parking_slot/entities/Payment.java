package com.system.design.low_level_design.case_studies.parking_slot.entities;

import com.system.design.low_level_design.case_studies.parking_slot.enums.PaymentMethod;
import com.system.design.low_level_design.case_studies.parking_slot.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a payment transaction for a parking ticket.
 */
public class Payment {

    private final String paymentId;
    private final String ticketId;
    private final BigDecimal amount;
    private final PaymentMethod method;
    private final Instant createdAt;
    private final String idempotencyKey;

    private PaymentStatus status;
    private String providerRef;
    private Instant paidAt;
    private String failureReason;

    public Payment(String ticketId, BigDecimal amount, PaymentMethod method, String idempotencyKey) {
        this.paymentId = UUID.randomUUID().toString();
        this.ticketId = ticketId;
        this.amount = amount;
        this.method = method;
        this.idempotencyKey = idempotencyKey;
        this.status = PaymentStatus.INITIATED;
        this.createdAt = Instant.now();
    }

    public void markSuccess(String providerRef) {
        this.providerRef = providerRef;
        this.status = PaymentStatus.SUCCESS;
        this.paidAt = Instant.now();
    }

    public void markFailed(String reason) {
        this.failureReason = reason;
        this.status = PaymentStatus.FAILED;
    }

    public void markRefunded() {
        this.status = PaymentStatus.REFUNDED;
    }

    // Getters
    public String getPaymentId()      { return paymentId; }
    public String getTicketId()       { return ticketId; }
    public BigDecimal getAmount()     { return amount; }
    public PaymentMethod getMethod()  { return method; }
    public PaymentStatus getStatus()  { return status; }
    public String getProviderRef()    { return providerRef; }
    public Instant getPaidAt()        { return paidAt; }
    public String getFailureReason()  { return failureReason; }
    public Instant getCreatedAt()     { return createdAt; }
    public String getIdempotencyKey() { return idempotencyKey; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payment)) return false;
        return Objects.equals(paymentId, ((Payment) o).paymentId);
    }

    @Override
    public int hashCode() { return Objects.hash(paymentId); }

    @Override
    public String toString() {
        return "Payment{paymentId='" + paymentId + "', amount=" + amount + ", status=" + status + "}";
    }
}

