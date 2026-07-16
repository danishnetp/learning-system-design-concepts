package com.system.design.low_level_design.case_studies.parking_slot.service;

import com.system.design.low_level_design.case_studies.parking_slot.entities.ParkingLot;
import com.system.design.low_level_design.case_studies.parking_slot.entities.ParkingSlot;
import com.system.design.low_level_design.case_studies.parking_slot.entities.Reservation;
import com.system.design.low_level_design.case_studies.parking_slot.enums.ReservationStatus;
import com.system.design.low_level_design.case_studies.parking_slot.enums.SlotStatus;
import com.system.design.low_level_design.case_studies.parking_slot.exceptions.InvalidRequestException;
import com.system.design.low_level_design.case_studies.parking_slot.exceptions.ParkingException;
import com.system.design.low_level_design.case_studies.parking_slot.exceptions.SlotUnavailableException;
import com.system.design.low_level_design.case_studies.parking_slot.interfaces.SlotAllocationStrategy;
import com.system.design.low_level_design.case_studies.parking_slot.models.AllocationContext;
import com.system.design.low_level_design.case_studies.parking_slot.models.CreateReservationRequest;
import com.system.design.low_level_design.case_studies.parking_slot.repositories.LotRepository;
import com.system.design.low_level_design.case_studies.parking_slot.repositories.ReservationRepository;
import com.system.design.low_level_design.case_studies.parking_slot.repositories.SlotRepository;

import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;

/**
 * Manages the full reservation lifecycle: create, confirm, check-in, cancel, expire, no-show.
 */
public class ReservationService {

    private static final Logger LOG = Logger.getLogger(ReservationService.class.getName());
    private static final int MAX_RESERVATIONS_PER_USER = 3;

    private final ReservationRepository reservationRepository;
    private final LotRepository lotRepository;
    private final SlotRepository slotRepository;
    private final SlotAllocationStrategy allocationStrategy;

    public ReservationService(ReservationRepository reservationRepository,
                              LotRepository lotRepository,
                              SlotRepository slotRepository,
                              SlotAllocationStrategy allocationStrategy) {
        this.reservationRepository = reservationRepository;
        this.lotRepository = lotRepository;
        this.slotRepository = slotRepository;
        this.allocationStrategy = allocationStrategy;
    }

    /**
     * Creates and confirms a reservation by pre-assigning a slot.
     */
    public Reservation create(CreateReservationRequest request) {
        // Validate lot exists and is active
        ParkingLot lot = lotRepository.findById(request.getLotId())
                .orElseThrow(() -> new InvalidRequestException("Lot not found: " + request.getLotId()));
        if (!lot.isActive()) {
            throw new InvalidRequestException("Lot is not active: " + request.getLotId());
        }

        // Validate time window
        if (request.getReservedFrom().isBefore(Instant.now())) {
            throw new InvalidRequestException("Reservation start time must be in the future");
        }
        if (!request.getReservedTo().isAfter(request.getReservedFrom())) {
            throw new InvalidRequestException("Reservation end time must be after start time");
        }

        // Enforce per-user reservation limit
        long activeCount = reservationRepository.findByStatus(ReservationStatus.CONFIRMED).stream()
                .filter(r -> r.getUserId().equals(request.getUserId()))
                .count();
        if (activeCount >= MAX_RESERVATIONS_PER_USER) {
            throw new ParkingException("User has reached max active reservations: " + MAX_RESERVATIONS_PER_USER);
        }

        // Find an available slot
        AllocationContext ctx = new AllocationContext(lot, request.getVehicleType(), request.isRequiresCharger());
        ParkingSlot slot = allocationStrategy.allocate(ctx)
                .orElseThrow(() -> new SlotUnavailableException(
                        "No available slot for " + request.getVehicleType() + " in lot " + request.getLotId()));

        // Atomically reserve the slot
        if (!slot.reserveIfAvailable()) {
            throw new SlotUnavailableException("Slot was taken concurrently: " + slot.getSlotId());
        }
        slotRepository.update(slot);

        // Create and confirm reservation
        Reservation reservation = new Reservation(
                request.getUserId(), request.getLotId(),
                request.getVehicleType(), request.getReservedFrom(), request.getReservedTo());
        reservation.confirm(slot.getSlotId());
        reservationRepository.save(reservation);

        LOG.info("Reservation created: " + reservation.getReservationId() + " -> slot " + slot.getSlotId());
        return reservation;
    }

    /**
     * Confirms check-in for a reservation. Called when the vehicle actually enters.
     *
     * @param reservationId the reservation
     * @param ticketId      the ticket generated at entry
     */
    public Reservation checkIn(String reservationId, String ticketId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ParkingException("Reservation not found: " + reservationId));

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new ParkingException("Cannot check in reservation in state: " + reservation.getStatus());
        }

        // Check the 15-minute hold window
        if (!reservation.isHoldWindowActive()) {
            expireReservation(reservationId);
            throw new ParkingException("Reservation hold window expired for: " + reservationId);
        }

        reservation.checkIn(ticketId);
        reservationRepository.update(reservation);
        LOG.info("Checked in reservation: " + reservationId + " with ticket: " + ticketId);
        return reservation;
    }

    /**
     * Cancels a reservation and frees the reserved slot.
     */
    public void cancel(String reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ParkingException("Reservation not found: " + reservationId));

        if (reservation.getStatus().isTerminal()) {
            throw new ParkingException("Cannot cancel reservation in terminal state: " + reservation.getStatus());
        }

        freeReservedSlot(reservation);
        reservation.cancel();
        reservationRepository.update(reservation);
        LOG.info("Reservation cancelled: " + reservationId);
    }

    /**
     * Marks a reservation as no-show and releases the slot.
     */
    public void markNoShow(String reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ParkingException("Reservation not found: " + reservationId));

        freeReservedSlot(reservation);
        reservation.markNoShow();
        reservationRepository.update(reservation);
        LOG.info("No-show marked for reservation: " + reservationId);
    }

    /**
     * Expires all confirmed reservations whose hold window has passed.
     * Should be invoked periodically by a scheduler job.
     */
    public void expireStaleReservations() {
        List<Reservation> confirmed = reservationRepository.findByStatus(ReservationStatus.CONFIRMED);
        for (Reservation r : confirmed) {
            if (!r.isHoldWindowActive()) {
                expireReservation(r.getReservationId());
            }
        }
    }

    private void expireReservation(String reservationId) {
        Reservation r = reservationRepository.findById(reservationId).orElseThrow();
        freeReservedSlot(r);
        r.expire();
        reservationRepository.update(r);
        LOG.info("Reservation expired: " + reservationId);
    }

    private void freeReservedSlot(Reservation reservation) {
        if (reservation.getAssignedSlotId() != null) {
            slotRepository.findById(reservation.getAssignedSlotId()).ifPresent(slot -> {
                if (slot.getStatus() == SlotStatus.RESERVED) {
                    slot.release();
                    slotRepository.update(slot);
                }
            });
        }
    }
}

