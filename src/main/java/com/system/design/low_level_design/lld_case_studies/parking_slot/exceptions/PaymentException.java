package com.system.design.low_level_design.lld_case_studies.parking_slot.exceptions;

/** Thrown on payment failure or invalid payment state. */
public class PaymentException extends ParkingException {
    private final boolean retryable;

    public PaymentException(String message, boolean retryable) {
        super(message);
        this.retryable = retryable;
    }

    public PaymentException(String message, boolean retryable, Throwable cause) {
        super(message, cause);
        this.retryable = retryable;
    }

    public boolean isRetryable() { return retryable; }
}

