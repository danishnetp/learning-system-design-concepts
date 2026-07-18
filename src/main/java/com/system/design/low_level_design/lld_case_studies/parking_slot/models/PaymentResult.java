package com.system.design.low_level_design.lld_case_studies.parking_slot.models;

import com.system.design.low_level_design.lld_case_studies.parking_slot.enums.PaymentStatus;

import java.math.BigDecimal;

/**
 * Result of a payment operation.
 */
public class PaymentResult {

    private final PaymentStatus status;
    private final String paymentId;
    private final String providerRef;
    private final BigDecimal amount;
    private final String errorMessage;
    private final boolean retryable;

    private PaymentResult(PaymentStatus status, String paymentId, String providerRef,
                          BigDecimal amount, String errorMessage, boolean retryable) {
        this.status = status;
        this.paymentId = paymentId;
        this.providerRef = providerRef;
        this.amount = amount;
        this.errorMessage = errorMessage;
        this.retryable = retryable;
    }

    public static PaymentResult success(String paymentId, String providerRef, BigDecimal amount) {
        return new PaymentResult(PaymentStatus.SUCCESS, paymentId, providerRef, amount, null, false);
    }

    public static PaymentResult failure(String paymentId, String error, boolean retryable) {
        return new PaymentResult(PaymentStatus.FAILED, paymentId, null, null, error, retryable);
    }

    public boolean isSuccess()         { return status == PaymentStatus.SUCCESS; }
    public PaymentStatus getStatus()   { return status; }
    public String getPaymentId()       { return paymentId; }
    public String getProviderRef()     { return providerRef; }
    public BigDecimal getAmount()      { return amount; }
    public String getErrorMessage()    { return errorMessage; }
    public boolean isRetryable()       { return retryable; }

    @Override
    public String toString() {
        return "PaymentResult{status=" + status + ", paymentId='" + paymentId + "', amount=" + amount + "}";
    }
}

