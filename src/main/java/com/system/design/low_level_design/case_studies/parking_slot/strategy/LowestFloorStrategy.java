package com.system.design.low_level_design.case_studies.parking_slot.strategy;

import com.system.design.low_level_design.case_studies.parking_slot.entities.Floor;
import com.system.design.low_level_design.case_studies.parking_slot.entities.ParkingSlot;
import com.system.design.low_level_design.case_studies.parking_slot.enums.SlotStatus;
import com.system.design.low_level_design.case_studies.parking_slot.interfaces.SlotAllocationStrategy;
import com.system.design.low_level_design.case_studies.parking_slot.models.AllocationContext;

import java.util.Comparator;
import java.util.Optional;

/**
 * Allocates the first available slot on the lowest floor number.
 * Best for walk-in cases where users prefer ground-floor access.
 */
public class LowestFloorStrategy implements SlotAllocationStrategy {

    @Override
    public Optional<ParkingSlot> allocate(AllocationContext context) {
        return context.getLot().getFloors().stream()
                .sorted(Comparator.comparingInt(Floor::getFloorNumber))
                .flatMap(floor -> floor.getSlots().stream())
                .filter(slot -> slot.getStatus() == SlotStatus.AVAILABLE)
                .filter(slot -> slot.supports(context.getVehicleType()))
                .filter(slot -> !context.isRequiresCharger() || slot.isHasCharger())
                .findFirst();
    }

    @Override
    public String strategyName() { return "LOWEST_FLOOR"; }
}

