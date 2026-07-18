package com.system.design.low_level_design.lld_case_studies.parking_slot.repositories;

import com.system.design.low_level_design.lld_case_studies.parking_slot.entities.Reservation;
import com.system.design.low_level_design.lld_case_studies.parking_slot.enums.ReservationStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Reservation persistence.
 */
public interface ReservationRepository {

    Reservation save(Reservation reservation);

    Optional<Reservation> findById(String reservationId);

    /**
     * Finds active reservations for a lot that overlap with the given window.
     */
    List<Reservation> findActiveForLotInWindow(String lotId, Instant from, Instant to);

    /**
     * Finds all reservations in a given status.
     */
    List<Reservation> findByStatus(ReservationStatus status);

    Reservation update(Reservation reservation);
}

