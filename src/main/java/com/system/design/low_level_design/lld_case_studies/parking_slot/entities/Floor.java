package com.system.design.low_level_design.lld_case_studies.parking_slot.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents one floor/zone within a parking lot.
 */
public class Floor {

    private final String floorId;
    private final String lotId;
    private final String label;   // e.g., "G", "1", "2", "B1"
    private final int floorNumber; // 0 = ground, -1 = basement, etc.
    private final List<ParkingSlot> slots;

    public Floor(String floorId, String lotId, String label, int floorNumber) {
        this.floorId = floorId;
        this.lotId = lotId;
        this.label = label;
        this.floorNumber = floorNumber;
        this.slots = new ArrayList<>();
    }

    public void addSlot(ParkingSlot slot) {
        slots.add(slot);
    }

    public String getFloorId()         { return floorId; }
    public String getLotId()           { return lotId; }
    public String getLabel()           { return label; }
    public int getFloorNumber()        { return floorNumber; }
    public List<ParkingSlot> getSlots() { return List.copyOf(slots); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Floor)) return false;
        return Objects.equals(floorId, ((Floor) o).floorId);
    }

    @Override
    public int hashCode() { return Objects.hash(floorId); }

    @Override
    public String toString() {
        return "Floor{floorId='" + floorId + "', label='" + label + "', slots=" + slots.size() + "}";
    }
}

