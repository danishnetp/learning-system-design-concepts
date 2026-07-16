package com.system.design.low_level_design.case_studies.parking_slot.entities;

import com.system.design.low_level_design.case_studies.parking_slot.enums.VehicleType;

import java.util.Objects;

/**
 * Represents a vehicle entering the parking system.
 */
public class Vehicle {

    private final String vehicleId;
    private final String licensePlate;
    private final VehicleType vehicleType;
    private final String ownerId;

    public Vehicle(String vehicleId, String licensePlate, VehicleType vehicleType, String ownerId) {
        this.vehicleId = vehicleId;
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType;
        this.ownerId = ownerId;
    }

    public String getVehicleId()      { return vehicleId; }
    public String getLicensePlate()   { return licensePlate; }
    public VehicleType getVehicleType() { return vehicleType; }
    public String getOwnerId()        { return ownerId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehicle)) return false;
        return Objects.equals(licensePlate, ((Vehicle) o).licensePlate);
    }

    @Override
    public int hashCode() { return Objects.hash(licensePlate); }

    @Override
    public String toString() {
        return "Vehicle{plate='" + licensePlate + "', type=" + vehicleType + "}";
    }
}

