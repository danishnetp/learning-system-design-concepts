package com.system.design.low_level_design.case_studies.parking_slot.enums;

/**
 * Lifecycle status of a parking reservation.
 */
public enum ReservationStatus {
    CREATED,
    CONFIRMED,
    CHECKED_IN,
    EXPIRED,
    CANCELLED,
    NO_SHOW;

    public boolean isActive() {
        return this == CREATED || this == CONFIRMED;
    }

    public boolean isTerminal() {
        return this == EXPIRED || this == CANCELLED || this == NO_SHOW || this == CHECKED_IN;
    }
}

