package com.system.design.low_level_design.lld_case_studies.parking_slot.enums;

/**
 * Lifecycle status of a parking ticket.
 */
public enum TicketStatus {
    ACTIVE,
    PAYMENT_PENDING,
    PAID,
    CLOSED,
    LOST,
    CANCELLED;

    public boolean isTerminal() {
        return this == CLOSED || this == CANCELLED;
    }

    public boolean isOpenForExit() {
        return this == ACTIVE || this == PAYMENT_PENDING;
    }
}

