package com.system.design.low_level_design.case_studies.parking_slot.repositories;

import com.system.design.low_level_design.case_studies.parking_slot.entities.ParkingTicket;
import com.system.design.low_level_design.case_studies.parking_slot.enums.TicketStatus;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ParkingTicket persistence.
 */
public interface TicketRepository {

    ParkingTicket save(ParkingTicket ticket);

    Optional<ParkingTicket> findById(String ticketId);

    /**
     * Finds currently active ticket for a given license plate in a lot.
     */
    Optional<ParkingTicket> findActiveByPlate(String licensePlate, String lotId);

    /**
     * Finds all tickets in a given status.
     */
    List<ParkingTicket> findByStatus(TicketStatus status);

    ParkingTicket update(ParkingTicket ticket);
}

