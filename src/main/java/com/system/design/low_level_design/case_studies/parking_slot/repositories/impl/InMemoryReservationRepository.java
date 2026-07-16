package com.system.design.low_level_design.case_studies.parking_slot.repositories.impl;

import com.system.design.low_level_design.case_studies.parking_slot.entities.Reservation;
import com.system.design.low_level_design.case_studies.parking_slot.enums.ReservationStatus;
import com.system.design.low_level_design.case_studies.parking_slot.repositories.ReservationRepository;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryReservationRepository implements ReservationRepository {

    private final Map<String, Reservation> store = new HashMap<>();

    @Override public Reservation save(Reservation r)   { store.put(r.getReservationId(), r); return r; }
    @Override public Optional<Reservation> findById(String id) { return Optional.ofNullable(store.get(id)); }
    @Override public Reservation update(Reservation r) { store.put(r.getReservationId(), r); return r; }

    @Override
    public List<Reservation> findActiveForLotInWindow(String lotId, Instant from, Instant to) {
        return store.values().stream()
                .filter(r -> r.getLotId().equals(lotId))
                .filter(r -> r.getStatus().isActive())
                .filter(r -> r.getReservedFrom().isBefore(to) && r.getReservedTo().isAfter(from))
                .collect(Collectors.toList());
    }

    @Override
    public List<Reservation> findByStatus(ReservationStatus status) {
        return store.values().stream()
                .filter(r -> r.getStatus() == status)
                .collect(Collectors.toList());
    }
}

