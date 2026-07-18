package com.system.design.low_level_design.lld_case_studies.parking_slot.service;

import com.system.design.low_level_design.lld_case_studies.parking_slot.entities.Payment;
import com.system.design.low_level_design.lld_case_studies.parking_slot.entities.ParkingLot;
import com.system.design.low_level_design.lld_case_studies.parking_slot.entities.ParkingSlot;
import com.system.design.low_level_design.lld_case_studies.parking_slot.entities.ParkingTicket;
import com.system.design.low_level_design.lld_case_studies.parking_slot.entities.Reservation;
import com.system.design.low_level_design.lld_case_studies.parking_slot.enums.SlotStatus;
import com.system.design.low_level_design.lld_case_studies.parking_slot.enums.VehicleType;
import com.system.design.low_level_design.lld_case_studies.parking_slot.exceptions.InvalidRequestException;
import com.system.design.low_level_design.lld_case_studies.parking_slot.exceptions.SlotUnavailableException;
import com.system.design.low_level_design.lld_case_studies.parking_slot.exceptions.TicketNotFoundException;
import com.system.design.low_level_design.lld_case_studies.parking_slot.interfaces.PricingPolicy;
import com.system.design.low_level_design.lld_case_studies.parking_slot.interfaces.SlotAllocationStrategy;
import com.system.design.low_level_design.lld_case_studies.parking_slot.models.AllocationContext;
import com.system.design.low_level_design.lld_case_studies.parking_slot.models.ExitSummary;
import com.system.design.low_level_design.lld_case_studies.parking_slot.models.Money;
import com.system.design.low_level_design.lld_case_studies.parking_slot.models.ParkRequest;
import com.system.design.low_level_design.lld_case_studies.parking_slot.models.SlotView;
import com.system.design.low_level_design.lld_case_studies.parking_slot.models.TicketSnapshot;
import com.system.design.low_level_design.lld_case_studies.parking_slot.models.UnparkRequest;
import com.system.design.low_level_design.lld_case_studies.parking_slot.repositories.LotRepository;
import com.system.design.low_level_design.lld_case_studies.parking_slot.repositories.ReservationRepository;
import com.system.design.low_level_design.lld_case_studies.parking_slot.repositories.SlotRepository;
import com.system.design.low_level_design.lld_case_studies.parking_slot.repositories.TicketRepository;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Main orchestrator for parking operations.
 *
 * <p>Coordinates: validation → allocation → ticketing → pricing → payment → release.
 * Each step is delegated to a specialist service or strategy so this class stays thin.
 */
public class ParkingService {

    private static final Logger LOG = Logger.getLogger(ParkingService.class.getName());

    private final LotRepository lotRepository;
    private final SlotRepository slotRepository;
    private final TicketRepository ticketRepository;
    private final ReservationRepository reservationRepository;
    private final SlotAllocationStrategy allocationStrategy;
    private final PricingPolicy pricingPolicy;
    private final PaymentService paymentService;
    private final ReservationService reservationService;
    private final ParkingValidator validator;

    public ParkingService(LotRepository lotRepository,
                          SlotRepository slotRepository,
                          TicketRepository ticketRepository,
                          ReservationRepository reservationRepository,
                          SlotAllocationStrategy allocationStrategy,
                          PricingPolicy pricingPolicy,
                          PaymentService paymentService,
                          ReservationService reservationService,
                          ParkingValidator validator) {
        this.lotRepository = lotRepository;
        this.slotRepository = slotRepository;
        this.ticketRepository = ticketRepository;
        this.reservationRepository = reservationRepository;
        this.allocationStrategy = allocationStrategy;
        this.pricingPolicy = pricingPolicy;
        this.paymentService = paymentService;
        this.reservationService = reservationService;
        this.validator = validator;
    }

    // ─────────────────────────────────────────────────────────────
    // ENTRY FLOW
    // ─────────────────────────────────────────────────────────────

    /**
     * Entry point: parks a vehicle and returns an active ticket.
     *
     * <p>Supports both walk-in and reservation-based entry.
     */
    public ParkingTicket park(ParkRequest request) {
        String plate = validator.normalizePlate(request.getLicensePlate());

        ParkingLot lot = lotRepository.findById(request.getLotId())
                .orElseThrow(() -> new InvalidRequestException("Lot not found: " + request.getLotId()));

        validator.validateParkRequest(request, lot);

        // Prevent duplicate active ticket for same plate in same lot
        Optional<ParkingTicket> existing = ticketRepository.findActiveByPlate(plate, request.getLotId());
        if (existing.isPresent()) {
            LOG.warning("Duplicate entry detected for plate " + plate + "; returning existing ticket");
            return existing.get();
        }

        ParkingSlot slot = resolveSlot(request, lot, plate);
        ParkingTicket ticket = new ParkingTicket(
                lot.getLotId(), slot.getSlotId(), plate,
                request.getVehicleType(), request.getEntryGateId(),
                pricingPolicy.policyVersion());

        ticketRepository.save(ticket);

        // Link reservation to check-in if applicable
        if (request.isReservation()) {
            reservationService.checkIn(request.getReservationId(), ticket.getTicketId());
        }

        LOG.info("Vehicle parked: " + plate + " -> slot " + slot.getSlotId() + " ticket=" + ticket.getTicketId());
        return ticket;
    }

    // ─────────────────────────────────────────────────────────────
    // EXIT FLOW
    // ─────────────────────────────────────────────────────────────

    /**
     * Exit point: computes fare, processes payment, releases slot, closes ticket.
     */
    public ExitSummary unpark(UnparkRequest request) {
        validator.validateUnparkRequest(request);

        ParkingTicket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found: " + request.getTicketId()));

        if (!ticket.getStatus().isOpenForExit()) {
            throw new InvalidRequestException("Ticket is not open for exit: state=" + ticket.getStatus());
        }

        // Compute fare
        Instant exitTime = Instant.now();
        TicketSnapshot snapshot = new TicketSnapshot(ticket, exitTime);
        Money fare = pricingPolicy.calculate(snapshot);

        ticket.markPaymentPending(fare.getAmount());
        ticketRepository.update(ticket);

        // Charge payment (idempotent)
        String paymentKey = request.getIdempotencyKey();
        Payment payment = paymentService.charge(ticket.getTicketId(), fare, request.getPaymentMethod(), paymentKey);

        // Mark ticket paid and closed
        ticket.markPaid(payment.getPaymentId());
        ticket.markClosed();
        ticketRepository.update(ticket);

        // Release the slot
        slotRepository.findById(ticket.getSlotId()).ifPresent(slot -> {
            slot.release();
            slotRepository.update(slot);
        });

        ExitSummary summary = new ExitSummary(
                ticket.getTicketId(), ticket.getSlotId(), ticket.getVehiclePlate(),
                ticket.getEntryTime(), exitTime,
                fare.getAmount(), payment.getPaymentId(),
                "Exit successful. Have a safe journey!");

        LOG.info("Vehicle exited: " + summary);
        return summary;
    }

    // ─────────────────────────────────────────────────────────────
    // AVAILABILITY
    // ─────────────────────────────────────────────────────────────

    /**
     * Returns current slot availability for a lot, optionally filtered by vehicle type.
     */
    public SlotView getAvailability(String lotId, VehicleType filterType) {
        List<ParkingSlot> allSlots = slotRepository.findByLotId(lotId);

        List<ParkingSlot> available = allSlots.stream()
                .filter(s -> s.getStatus() == SlotStatus.AVAILABLE)
                .filter(s -> filterType == null || s.supports(filterType))
                .collect(Collectors.toList());

        Map<VehicleType, Long> countByType = new EnumMap<>(VehicleType.class);
        for (VehicleType type : VehicleType.values()) {
            long count = available.stream().filter(s -> s.supports(type)).count();
            if (count > 0) countByType.put(type, count);
        }

        return new SlotView(lotId, countByType, available);
    }

    /**
     * Returns a fare estimate for an active ticket without processing payment.
     */
    public Money estimateFare(String ticketId) {
        ParkingTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found: " + ticketId));
        TicketSnapshot snapshot = new TicketSnapshot(ticket, Instant.now());
        return pricingPolicy.calculate(snapshot);
    }

    /**
     * Marks a ticket as LOST and closes it with a fixed lost-ticket fine.
     */
    public void reportLostTicket(String ticketId) {
        ParkingTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found: " + ticketId));

        if (!ticket.getStatus().isOpenForExit()) {
            throw new InvalidRequestException("Ticket cannot be reported lost in state: " + ticket.getStatus());
        }

        ticket.markLost();
        ticketRepository.update(ticket);

        // Free the slot even on lost ticket
        slotRepository.findById(ticket.getSlotId()).ifPresent(slot -> {
            slot.release();
            slotRepository.update(slot);
        });

        LOG.warning("Ticket reported as lost: " + ticketId + " slot released: " + ticket.getSlotId());
    }

    /**
     * Cancels an active ticket (e.g., vehicle never entered after ticket issued).
     */
    public void cancelTicket(String ticketId) {
        ParkingTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found: " + ticketId));

        if (ticket.getStatus().isTerminal()) {
            throw new InvalidRequestException("Ticket already in terminal state: " + ticket.getStatus());
        }

        ticket.markCancelled();
        ticketRepository.update(ticket);

        slotRepository.findById(ticket.getSlotId()).ifPresent(slot -> {
            slot.release();
            slotRepository.update(slot);
        });

        LOG.info("Ticket cancelled: " + ticketId);
    }

    // ─────────────────────────────────────────────────────────────
    // LOT MANAGEMENT
    // ─────────────────────────────────────────────────────────────

    /**
     * Registers a new parking lot (admin operation).
     */
    public ParkingLot registerLot(ParkingLot lot) {
        return lotRepository.save(lot);
    }

    /**
     * Registers a new slot to an existing lot (admin operation).
     */
    public ParkingSlot registerSlot(ParkingSlot slot) {
        return slotRepository.save(slot);
    }

    /**
     * Returns current status of a specific ticket.
     */
    public Optional<ParkingTicket> getTicket(String ticketId) {
        return ticketRepository.findById(ticketId);
    }

    // ─────────────────────────────────────────────────────────────
    // INTERNAL HELPERS
    // ─────────────────────────────────────────────────────────────

    /**
     * Resolves a slot for walk-in or reservation entry, then atomically marks it occupied.
     */
    private ParkingSlot resolveSlot(ParkRequest request, ParkingLot lot, String plate) {
        if (request.isReservation()) {
            return resolveReservedSlot(request.getReservationId(), plate);
        }
        return allocateNewSlot(lot, request);
    }

    private ParkingSlot resolveReservedSlot(String reservationId, @SuppressWarnings("unused") String plate) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new InvalidRequestException("Reservation not found: " + reservationId));

        if (!reservation.getStatus().isActive()) {
            throw new InvalidRequestException("Reservation is no longer active: " + reservation.getStatus());
        }

        ParkingSlot slot = slotRepository.findById(reservation.getAssignedSlotId())
                .orElseThrow(() -> new SlotUnavailableException("Reserved slot not found: " + reservation.getAssignedSlotId()));

        // Transition: RESERVED -> OCCUPIED
        if (slot.getStatus() == SlotStatus.RESERVED) {
            slot.setStatus(SlotStatus.OCCUPIED);
            slotRepository.update(slot);
        } else {
            throw new SlotUnavailableException("Reserved slot is not in RESERVED state: " + slot.getStatus());
        }
        return slot;
    }

    private ParkingSlot allocateNewSlot(ParkingLot lot, ParkRequest request) {
        AllocationContext ctx = new AllocationContext(lot, request.getVehicleType(), request.isRequiresCharger());
        ParkingSlot slot = allocationStrategy.allocate(ctx)
                .orElseThrow(() -> new SlotUnavailableException(
                        "No available slot for " + request.getVehicleType() + " in lot " + request.getLotId()));

        // Atomically occupy - handles concurrency race
        if (!slot.occupyIfAvailable()) {
            throw new SlotUnavailableException("Slot was taken concurrently. Please retry.");
        }
        slotRepository.update(slot);
        return slot;
    }
}

