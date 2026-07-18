package com.system.design.low_level_design.lld_case_studies.parking_slot.strategy;

import com.system.design.low_level_design.lld_case_studies.parking_slot.entities.ParkingSlot;
import com.system.design.low_level_design.lld_case_studies.parking_slot.enums.SlotStatus;
import com.system.design.low_level_design.lld_case_studies.parking_slot.interfaces.SlotAllocationStrategy;
import com.system.design.low_level_design.lld_case_studies.parking_slot.models.AllocationContext;

import java.util.Optional;

/**
 * Allocates the first available slot in sequential order.
 * Simulates nearest-to-entry assignment where slots are ordered by proximity in floors list.
 */
public class NearestEntryStrategy implements SlotAllocationStrategy {

    @Override
    public Optional<ParkingSlot> allocate(AllocationContext context) {
        return context.getLot().getFloors().stream()
                .flatMap(floor -> floor.getSlots().stream())
                .filter(slot -> slot.getStatus() == SlotStatus.AVAILABLE)
                .filter(slot -> slot.supports(context.getVehicleType()))
                .filter(slot -> !context.isRequiresCharger() || slot.isHasCharger())
                .findFirst();
    }

    @Override
    public String strategyName() { return "NEAREST_ENTRY"; }
}

