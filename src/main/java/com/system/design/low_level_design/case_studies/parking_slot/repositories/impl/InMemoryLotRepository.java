package com.system.design.low_level_design.case_studies.parking_slot.repositories.impl;

import com.system.design.low_level_design.case_studies.parking_slot.entities.ParkingLot;
import com.system.design.low_level_design.case_studies.parking_slot.repositories.LotRepository;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryLotRepository implements LotRepository {

    private final Map<String, ParkingLot> store = new HashMap<>();

    @Override public ParkingLot save(ParkingLot lot) { store.put(lot.getLotId(), lot); return lot; }

    @Override public Optional<ParkingLot> findById(String id) { return Optional.ofNullable(store.get(id)); }

    @Override public List<ParkingLot> findAllActive() {
        return store.values().stream().filter(ParkingLot::isActive).collect(Collectors.toList());
    }

    @Override public ParkingLot update(ParkingLot lot) { store.put(lot.getLotId(), lot); return lot; }
}

