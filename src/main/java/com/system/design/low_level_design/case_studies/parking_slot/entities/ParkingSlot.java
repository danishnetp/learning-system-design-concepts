package com.system.design.low_level_design.case_studies.parking_slot.entities;

import com.system.design.low_level_design.case_studies.parking_slot.enums.SlotStatus;
import com.system.design.low_level_design.case_studies.parking_slot.enums.VehicleType;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a single physical parking slot.
 * Version field enables optimistic locking to prevent double-assignment.
 */
public class ParkingSlot {

    private final String slotId;
    private final String floorId;
    private final String lotId;
    private final String slotNumber;
    private final Set<VehicleType> supportedVehicleTypes;
    private final boolean hasCharger;
    private volatile SlotStatus status;
    private volatile long version; // optimistic lock version

    public ParkingSlot(String slotId, String floorId, String lotId, String slotNumber,
                       Set<VehicleType> supportedVehicleTypes, boolean hasCharger) {
        this.slotId = slotId;
        this.floorId = floorId;
        this.lotId = lotId;
        this.slotNumber = slotNumber;
        this.supportedVehicleTypes = EnumSet.copyOf(supportedVehicleTypes);
        this.hasCharger = hasCharger;
        this.status = SlotStatus.AVAILABLE;
        this.version = 0;
    }

    /**
     * Atomically occupy the slot if still AVAILABLE.
     *
     * @return true if this call claimed the slot; false if already taken.
     */
    public synchronized boolean occupyIfAvailable() {
        if (status != SlotStatus.AVAILABLE) return false;
        status = SlotStatus.OCCUPIED;
        version++;
        return true;
    }

    /**
     * Reserve slot (for pre-booking).
     */
    public synchronized boolean reserveIfAvailable() {
        if (status != SlotStatus.AVAILABLE) return false;
        status = SlotStatus.RESERVED;
        version++;
        return true;
    }

    /**
     * Release slot back to AVAILABLE.
     */
    public synchronized void release() {
        status = SlotStatus.AVAILABLE;
        version++;
    }

    public boolean supports(VehicleType type) {
        return supportedVehicleTypes.contains(type);
    }

    // Getters
    public String getSlotId()                        { return slotId; }
    public String getFloorId()                       { return floorId; }
    public String getLotId()                         { return lotId; }
    public String getSlotNumber()                    { return slotNumber; }
    public Set<VehicleType> getSupportedVehicleTypes() { return Set.copyOf(supportedVehicleTypes); }
    public boolean isHasCharger()                    { return hasCharger; }
    public SlotStatus getStatus()                    { return status; }
    public long getVersion()                         { return version; }

    public void setStatus(SlotStatus status) {
        this.status = status;
        this.version++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParkingSlot)) return false;
        return Objects.equals(slotId, ((ParkingSlot) o).slotId);
    }

    @Override
    public int hashCode() { return Objects.hash(slotId); }

    @Override
    public String toString() {
        return "ParkingSlot{slotId='" + slotId + "', slotNumber='" + slotNumber +
                "', status=" + status + ", hasCharger=" + hasCharger + "}";
    }
}

