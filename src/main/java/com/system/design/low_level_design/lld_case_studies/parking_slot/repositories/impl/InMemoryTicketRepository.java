package com.system.design.low_level_design.lld_case_studies.parking_slot.repositories.impl;

import com.system.design.low_level_design.lld_case_studies.parking_slot.entities.ParkingTicket;
import com.system.design.low_level_design.lld_case_studies.parking_slot.enums.TicketStatus;
import com.system.design.low_level_design.lld_case_studies.parking_slot.repositories.TicketRepository;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTicketRepository implements TicketRepository {

    private final Map<String, ParkingTicket> store = new HashMap<>();

    @Override public ParkingTicket save(ParkingTicket t)   { store.put(t.getTicketId(), t); return t; }
    @Override public Optional<ParkingTicket> findById(String id) { return Optional.ofNullable(store.get(id)); }
    @Override public ParkingTicket update(ParkingTicket t) { store.put(t.getTicketId(), t); return t; }

    @Override
    public Optional<ParkingTicket> findActiveByPlate(String plate, String lotId) {
        return store.values().stream()
                .filter(t -> t.getVehiclePlate().equalsIgnoreCase(plate))
                .filter(t -> t.getLotId().equals(lotId))
                .filter(t -> t.getStatus().isOpenForExit())
                .findFirst();
    }

    @Override
    public List<ParkingTicket> findByStatus(TicketStatus status) {
        return store.values().stream()
                .filter(t -> t.getStatus() == status)
                .collect(Collectors.toList());
    }
}

