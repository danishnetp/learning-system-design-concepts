package com.system.design.low_level_design.lld_case_studies.parking_slot.models;

import com.system.design.low_level_design.lld_case_studies.parking_slot.entities.ParkingLot;
import com.system.design.low_level_design.lld_case_studies.parking_slot.enums.VehicleType;

/**
 * Context passed to a SlotAllocationStrategy.
 */
public class AllocationContext {

    private final ParkingLot lot;
    private final VehicleType vehicleType;
    private final boolean requiresCharger;

    public AllocationContext(ParkingLot lot, VehicleType vehicleType, boolean requiresCharger) {
        this.lot = lot;
        this.vehicleType = vehicleType;
        this.requiresCharger = requiresCharger;
    }

    public ParkingLot getLot()             { return lot; }
    public VehicleType getVehicleType()    { return vehicleType; }
    public boolean isRequiresCharger()     { return requiresCharger; }
}

