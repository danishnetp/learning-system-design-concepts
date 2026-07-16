package com.system.design.low_level_design.case_studies.parking_slot.enums;

/**
 * Status of a physical parking slot.
 */
public enum SlotStatus {
    AVAILABLE,
    OCCUPIED,
    RESERVED,
    BLOCKED,
    OUT_OF_SERVICE;

    public boolean isUsable() {
        return this == AVAILABLE;
    }
}

