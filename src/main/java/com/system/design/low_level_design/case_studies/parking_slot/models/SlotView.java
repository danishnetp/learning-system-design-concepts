package com.system.design.low_level_design.case_studies.parking_slot.models;

import com.system.design.low_level_design.case_studies.parking_slot.entities.ParkingSlot;
import com.system.design.low_level_design.case_studies.parking_slot.enums.VehicleType;

import java.util.List;
import java.util.Map;

/**
 * Snapshot of lot availability for a given query.
 */
public class SlotView {

    private final String lotId;
    private final Map<VehicleType, Long> availableCountByType;
    private final List<ParkingSlot> availableSlots;

    public SlotView(String lotId, Map<VehicleType, Long> availableCountByType,
                    List<ParkingSlot> availableSlots) {
        this.lotId = lotId;
        this.availableCountByType = Map.copyOf(availableCountByType);
        this.availableSlots = List.copyOf(availableSlots);
    }

    public String getLotId()                               { return lotId; }
    public Map<VehicleType, Long> getAvailableCountByType() { return availableCountByType; }
    public List<ParkingSlot> getAvailableSlots()           { return availableSlots; }

    public long totalAvailable() {
        return availableCountByType.values().stream().mapToLong(Long::longValue).sum();
    }

    @Override
    public String toString() {
        return "SlotView{lotId='" + lotId + "', availability=" + availableCountByType + "}";
    }
}

