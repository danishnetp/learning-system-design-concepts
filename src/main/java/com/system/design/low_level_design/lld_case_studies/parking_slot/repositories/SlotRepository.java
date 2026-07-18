package com.system.design.low_level_design.lld_case_studies.parking_slot.repositories;

import com.system.design.low_level_design.lld_case_studies.parking_slot.entities.ParkingSlot;
import com.system.design.low_level_design.lld_case_studies.parking_slot.enums.VehicleType;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ParkingSlot persistence and atomic occupancy.
 */
public interface SlotRepository {

    ParkingSlot save(ParkingSlot slot);

    Optional<ParkingSlot> findById(String slotId);

    /**
     * Finds first available slot in a lot that supports the vehicle type.
     */
    Optional<ParkingSlot> findFirstAvailable(String lotId, VehicleType vehicleType, boolean requiresCharger);

    /**
     * Returns all slots in a lot.
     */
    List<ParkingSlot> findByLotId(String lotId);

    ParkingSlot update(ParkingSlot slot);
}

