package com.system.design.low_level_design.case_studies.parking_slot.repositories.impl;

import com.system.design.low_level_design.case_studies.parking_slot.entities.ParkingSlot;
import com.system.design.low_level_design.case_studies.parking_slot.enums.SlotStatus;
import com.system.design.low_level_design.case_studies.parking_slot.enums.VehicleType;
import com.system.design.low_level_design.case_studies.parking_slot.repositories.SlotRepository;

import java.util.*;
import java.util.stream.Collectors;

public class InMemorySlotRepository implements SlotRepository {

    private final Map<String, ParkingSlot> store = new HashMap<>();

    @Override public ParkingSlot save(ParkingSlot slot)   { store.put(slot.getSlotId(), slot); return slot; }
    @Override public Optional<ParkingSlot> findById(String id) { return Optional.ofNullable(store.get(id)); }
    @Override public ParkingSlot update(ParkingSlot slot) { store.put(slot.getSlotId(), slot); return slot; }

    @Override
    public Optional<ParkingSlot> findFirstAvailable(String lotId, VehicleType vehicleType, boolean requiresCharger) {
        return store.values().stream()
                .filter(s -> s.getLotId().equals(lotId))
                .filter(s -> s.getStatus() == SlotStatus.AVAILABLE)
                .filter(s -> s.supports(vehicleType))
                .filter(s -> !requiresCharger || s.isHasCharger())
                .findFirst();
    }

    @Override
    public List<ParkingSlot> findByLotId(String lotId) {
        return store.values().stream()
                .filter(s -> s.getLotId().equals(lotId))
                .collect(Collectors.toList());
    }
}

