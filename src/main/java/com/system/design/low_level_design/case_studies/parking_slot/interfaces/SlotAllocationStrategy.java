package com.system.design.low_level_design.case_studies.parking_slot.interfaces;

import com.system.design.low_level_design.case_studies.parking_slot.entities.ParkingSlot;
import com.system.design.low_level_design.case_studies.parking_slot.models.AllocationContext;

import java.util.Optional;

/**
 * Strategy for choosing a suitable parking slot.
 * Implementations vary by business policy (nearest, lowest floor, etc.).
 */
public interface SlotAllocationStrategy {

    /**
     * Selects the best available slot from the lot based on context.
     *
     * @param context lot, vehicle type, and charger requirements
     * @return chosen slot, or empty if none available
     */
    Optional<ParkingSlot> allocate(AllocationContext context);

    /**
     * Human-readable strategy name.
     */
    String strategyName();
}

