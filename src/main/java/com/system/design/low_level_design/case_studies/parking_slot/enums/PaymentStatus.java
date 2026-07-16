package com.system.design.low_level_design.case_studies.parking_slot.enums;

/**
 * Payment transaction status.
 */
public enum PaymentStatus {
    INITIATED,
    SUCCESS,
    FAILED,
    REFUNDED;

    public boolean isTerminal() {
        return this == SUCCESS || this == REFUNDED;
    }
}

