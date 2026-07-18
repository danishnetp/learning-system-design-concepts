package com.system.design.low_level_design.lld_case_studies.parking_slot.strategy;

import com.system.design.low_level_design.lld_case_studies.parking_slot.entities.ParkingSlot;
import com.system.design.low_level_design.lld_case_studies.parking_slot.enums.SlotStatus;
import com.system.design.low_level_design.lld_case_studies.parking_slot.interfaces.SlotAllocationStrategy;
import com.system.design.low_level_design.lld_case_studies.parking_slot.models.AllocationContext;
import com.system.design.low_level_design.lld_case_studies.parking_slot.repositories.TicketRepository;

import java.util.Comparator;
import java.util.Optional;

/**
 * Allocates the slot with the fewest past occupancies (usage count).
 * Helps distribute wear evenly across slots.
 * Falls back to nearest entry if usage data is unavailable.
 */
public class LeastUsedStrategy implements SlotAllocationStrategy {

    private final TicketRepository ticketRepository;

    public LeastUsedStrategy(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public Optional<ParkingSlot> allocate(AllocationContext context) {
        return context.getLot().getFloors().stream()
                .flatMap(floor -> floor.getSlots().stream())
                .filter(slot -> slot.getStatus() == SlotStatus.AVAILABLE)
                .filter(slot -> slot.supports(context.getVehicleType()))
                .filter(slot -> !context.isRequiresCharger() || slot.isHasCharger())
                .min(Comparator.comparingLong(slot -> usageCount(slot.getSlotId())));
    }

    private long usageCount(String slotId) {
        // In production: use a dedicated usage counter or analytics table
        // For demo: count all tickets (any status) in PAID or CLOSED state for this slot
        return ticketRepository.findByStatus(com.system.design.low_level_design.lld_case_studies.parking_slot.enums.TicketStatus.CLOSED)
                .stream()
                .filter(t -> t.getSlotId().equals(slotId))
                .count();
    }

    @Override
    public String strategyName() { return "LEAST_USED"; }
}

