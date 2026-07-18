package com.system.design.low_level_design.lld_case_studies.parking_slot.entities;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a physical parking lot.
 */
public class ParkingLot {

    private final String lotId;
    private final String name;
    private final String address;
    private final List<Floor> floors;
    private boolean active;
    private final Instant createdAt;

    public ParkingLot(String lotId, String name, String address) {
        this.lotId = lotId;
        this.name = name;
        this.address = address;
        this.floors = new ArrayList<>();
        this.active = true;
        this.createdAt = Instant.now();
    }

    public void addFloor(Floor floor) {
        floors.add(floor);
    }

    public String getLotId()       { return lotId; }
    public String getName()        { return name; }
    public String getAddress()     { return address; }
    public List<Floor> getFloors() { return List.copyOf(floors); }
    public boolean isActive()      { return active; }
    public Instant getCreatedAt()  { return createdAt; }

    public void setActive(boolean active) { this.active = active; }

    public long totalAvailableSlots() {
        return floors.stream()
                .flatMap(f -> f.getSlots().stream())
                .filter(s -> s.getStatus().isUsable())
                .count();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParkingLot)) return false;
        return Objects.equals(lotId, ((ParkingLot) o).lotId);
    }

    @Override
    public int hashCode() { return Objects.hash(lotId); }

    @Override
    public String toString() {
        return "ParkingLot{lotId='" + lotId + "', name='" + name + "', floors=" + floors.size() + "}";
    }
}

