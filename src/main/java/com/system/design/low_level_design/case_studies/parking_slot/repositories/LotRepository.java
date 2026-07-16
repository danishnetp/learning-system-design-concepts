package com.system.design.low_level_design.case_studies.parking_slot.repositories;

import com.system.design.low_level_design.case_studies.parking_slot.entities.ParkingLot;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ParkingLot persistence.
 */
public interface LotRepository {
    ParkingLot save(ParkingLot lot);
    Optional<ParkingLot> findById(String lotId);
    List<ParkingLot> findAllActive();
    ParkingLot update(ParkingLot lot);
}

