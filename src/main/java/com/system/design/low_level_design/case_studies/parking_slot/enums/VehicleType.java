package com.system.design.low_level_design.case_studies.parking_slot.enums;

/**
 * Supported vehicle categories.
 */
public enum VehicleType {
    BIKE,
    CAR,
    SUV,
    EV,
    TRUCK;

    /**
     * Returns a default slot size classification to help matching slots.
     * Higher = larger vehicle.
     */
    public int sizeRank() {
        return switch (this) {
            case BIKE -> 1;
            case CAR -> 2;
            case SUV -> 3;
            case EV  -> 2; // same physical size as CAR, just EV charger preferred
            case TRUCK -> 4;
        };
    }
}

