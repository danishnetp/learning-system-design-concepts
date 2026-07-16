package com.system.design.low_level_design.case_studies.parking_slot.example;

import com.system.design.low_level_design.case_studies.parking_slot.entities.Floor;
import com.system.design.low_level_design.case_studies.parking_slot.entities.ParkingLot;
import com.system.design.low_level_design.case_studies.parking_slot.entities.ParkingSlot;
import com.system.design.low_level_design.case_studies.parking_slot.entities.ParkingTicket;
import com.system.design.low_level_design.case_studies.parking_slot.entities.Reservation;
import com.system.design.low_level_design.case_studies.parking_slot.enums.PaymentMethod;
import com.system.design.low_level_design.case_studies.parking_slot.enums.VehicleType;
import com.system.design.low_level_design.case_studies.parking_slot.models.CreateReservationRequest;
import com.system.design.low_level_design.case_studies.parking_slot.models.ExitSummary;
import com.system.design.low_level_design.case_studies.parking_slot.models.Money;
import com.system.design.low_level_design.case_studies.parking_slot.models.ParkRequest;
import com.system.design.low_level_design.case_studies.parking_slot.models.SlotView;
import com.system.design.low_level_design.case_studies.parking_slot.models.UnparkRequest;
import com.system.design.low_level_design.case_studies.parking_slot.payment.MockPaymentGateway;
import com.system.design.low_level_design.case_studies.parking_slot.pricing.FlatRatePricingPolicy;
import com.system.design.low_level_design.case_studies.parking_slot.pricing.SlabPricingPolicy;
import com.system.design.low_level_design.case_studies.parking_slot.repositories.impl.*;
import com.system.design.low_level_design.case_studies.parking_slot.service.*;
import com.system.design.low_level_design.case_studies.parking_slot.strategy.LowestFloorStrategy;
import com.system.design.low_level_design.case_studies.parking_slot.strategy.NearestEntryStrategy;

import java.time.Instant;
import java.util.EnumSet;

/**
 * Runnable demo showing the complete parking slot LLD in action.
 *
 * Demonstrates:
 *  1. Lot setup (floors, slots)
 *  2. Walk-in park and exit
 *  3. EV vehicle with charger requirement
 *  4. Reservation flow (create → check-in via park → auto exit)
 *  5. Duplicate entry idempotency
 *  6. Availability query
 *  7. Lost ticket handling
 *  8. Fare estimation
 */
public class ParkingSystemExample {

    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("   Parking Slot LLD – Full Demo");
        System.out.println("═══════════════════════════════════════════════════\n");

        // ── 1. Wire up repositories ──────────────────────────────
        InMemoryLotRepository        lotRepo         = new InMemoryLotRepository();
        InMemorySlotRepository       slotRepo        = new InMemorySlotRepository();
        InMemoryTicketRepository     ticketRepo      = new InMemoryTicketRepository();
        InMemoryReservationRepository reservationRepo = new InMemoryReservationRepository();
        InMemoryPaymentRepository    paymentRepo     = new InMemoryPaymentRepository();

        // ── 2. Wire up services ──────────────────────────────────
        NearestEntryStrategy  allocationStrategy = new NearestEntryStrategy();
        FlatRatePricingPolicy pricingPolicy      = new FlatRatePricingPolicy(new Money(30)); // ₹30/hr
        MockPaymentGateway    gateway            = new MockPaymentGateway();
        PaymentService        paymentService     = new PaymentService(paymentRepo, gateway);
        ReservationService    reservationService = new ReservationService(
                reservationRepo, lotRepo, slotRepo, new LowestFloorStrategy());
        ParkingValidator      validator          = new ParkingValidator();

        ParkingService parkingService = new ParkingService(
                lotRepo, slotRepo, ticketRepo, reservationRepo,
                allocationStrategy, pricingPolicy,
                paymentService, reservationService, validator);

        // ── 3. Set up parking lot ────────────────────────────────
        ParkingLot lot = buildLot(lotRepo, slotRepo);
        System.out.println("✓ Lot created: " + lot.getName() +
                " | total available=" + lot.totalAvailableSlots() + "\n");

        // ── 4. Demo: Walk-in CAR ─────────────────────────────────
        System.out.println("━━━ Demo 1: Walk-in CAR park & exit ━━━");
        demoWalkIn(parkingService, lot.getLotId());

        // ── 5. Demo: Availability query ──────────────────────────
        System.out.println("\n━━━ Demo 2: Availability query ━━━");
        demoAvailability(parkingService, lot.getLotId());

        // ── 6. Demo: EV vehicle requiring charger ────────────────
        System.out.println("\n━━━ Demo 3: EV with charger requirement ━━━");
        demoEvParking(parkingService, lot.getLotId());

        // ── 7. Demo: Reservation flow ────────────────────────────
        System.out.println("\n━━━ Demo 4: Reservation → check-in ━━━");
        demoReservation(parkingService, reservationService, lot.getLotId());

        // ── 8. Demo: Slab pricing ────────────────────────────────
        System.out.println("\n━━━ Demo 5: Slab pricing fare estimate ━━━");
        demoSlabPricing(lotRepo, slotRepo, ticketRepo, reservationRepo, paymentRepo, paymentService, reservationService, validator);

        // ── 9. Demo: Lost ticket ─────────────────────────────────
        System.out.println("\n━━━ Demo 6: Lost ticket ━━━");
        demoLostTicket(parkingService, lot.getLotId());

        System.out.println("\n═══════════════════════════════════════════════════");
        System.out.println("   Demo Complete");
        System.out.println("═══════════════════════════════════════════════════");
    }

    // ─────────────────────────────────────────────────────────────
    // LOT SETUP
    // ─────────────────────────────────────────────────────────────

    private static ParkingLot buildLot(InMemoryLotRepository lotRepo, InMemorySlotRepository slotRepo) {
        ParkingLot lot = new ParkingLot("LOT-01", "Central Park", "MG Road, Bengaluru");

        // Ground floor: BIKE + CAR slots
        Floor gf = new Floor("FL-G", "LOT-01", "G", 0);
        for (int i = 1; i <= 5; i++) {
            ParkingSlot bikeSlot = new ParkingSlot(
                    "G-BK-" + i, "FL-G", "LOT-01", "G-BIKE-" + i,
                    EnumSet.of(VehicleType.BIKE), false);
            ParkingSlot carSlot = new ParkingSlot(
                    "G-CR-" + i, "FL-G", "LOT-01", "G-CAR-" + i,
                    EnumSet.of(VehicleType.CAR, VehicleType.SUV), false);
            gf.addSlot(bikeSlot);
            gf.addSlot(carSlot);
            slotRepo.save(bikeSlot);
            slotRepo.save(carSlot);
        }

        // Floor 1: CAR + EV (2 with charger)
        Floor f1 = new Floor("FL-1", "LOT-01", "1", 1);
        for (int i = 1; i <= 4; i++) {
            boolean hasCharger = i <= 2;
            ParkingSlot evSlot = new ParkingSlot(
                    "1-EV-" + i, "FL-1", "LOT-01", "1-EV-" + i,
                    EnumSet.of(VehicleType.EV, VehicleType.CAR), hasCharger);
            f1.addSlot(evSlot);
            slotRepo.save(evSlot);
        }

        // Floor 2: TRUCK + SUV
        Floor f2 = new Floor("FL-2", "LOT-01", "2", 2);
        for (int i = 1; i <= 3; i++) {
            ParkingSlot truckSlot = new ParkingSlot(
                    "2-TR-" + i, "FL-2", "LOT-01", "2-TRUCK-" + i,
                    EnumSet.of(VehicleType.TRUCK, VehicleType.SUV), false);
            f2.addSlot(truckSlot);
            slotRepo.save(truckSlot);
        }

        lot.addFloor(gf);
        lot.addFloor(f1);
        lot.addFloor(f2);
        lotRepo.save(lot);
        return lot;
    }

    // ─────────────────────────────────────────────────────────────
    // DEMOS
    // ─────────────────────────────────────────────────────────────

    private static void demoWalkIn(ParkingService ps, String lotId) {
        ParkRequest req = new ParkRequest(lotId, "KA01MJ1234", VehicleType.CAR, "GATE-A");

        ParkingTicket ticket = ps.park(req);
        System.out.println("✓ Parked: " + ticket);

        // Estimate fare
        Money estimate = ps.estimateFare(ticket.getTicketId());
        System.out.println("  Fare estimate: " + estimate + " (within grace period)");

        // Exit
        UnparkRequest unpark = new UnparkRequest(ticket.getTicketId(), "GATE-EXIT-A", PaymentMethod.UPI);
        ExitSummary summary = ps.unpark(unpark);
        System.out.println("✓ Exit summary: " + summary);
    }

    private static void demoAvailability(ParkingService ps, String lotId) {
        SlotView view = ps.getAvailability(lotId, null); // all types
        System.out.println("  Availability: " + view.getAvailableCountByType());
        System.out.println("  Total available slots: " + view.totalAvailable());

        SlotView carOnly = ps.getAvailability(lotId, VehicleType.CAR);
        System.out.println("  CAR slots available: " + carOnly.totalAvailable());
    }

    private static void demoEvParking(ParkingService ps, String lotId) {
        ParkRequest req = new ParkRequest(
                lotId, "KA05EV9999", VehicleType.EV, "GATE-B",
                "KA05EV9999-" + System.currentTimeMillis(), true, null); // requiresCharger=true

        ParkingTicket ticket = ps.park(req);
        System.out.println("✓ EV parked: " + ticket);

        UnparkRequest unpark = new UnparkRequest(ticket.getTicketId(), "GATE-EXIT-B", PaymentMethod.CARD);
        ExitSummary summary = ps.unpark(unpark);
        System.out.println("✓ EV exit: " + summary);
    }

    private static void demoReservation(@SuppressWarnings("unused") ParkingService ps, ReservationService rs, String lotId) {
        Instant from = Instant.now().plusSeconds(60);
        Instant to   = Instant.now().plusSeconds(3600);

        CreateReservationRequest createReq = new CreateReservationRequest(
                "user-alice", lotId, VehicleType.CAR, from, to, false);

        Reservation reservation = rs.create(createReq);
        System.out.println("✓ Reservation created: " + reservation);

        // Cancel and verify slot is freed
        rs.cancel(reservation.getReservationId());
        System.out.println("✓ Reservation cancelled: status=" + reservation.getStatus());
    }

    private static void demoSlabPricing(
            InMemoryLotRepository lotRepo, InMemorySlotRepository slotRepo,
            InMemoryTicketRepository ticketRepo, InMemoryReservationRepository reservationRepo,
            @SuppressWarnings("unused") InMemoryPaymentRepository paymentRepo, PaymentService paymentService,
            ReservationService reservationService, ParkingValidator validator) {

        // Create a fresh lot with slab pricing
        ParkingLot slabLot = new ParkingLot("LOT-SLAB", "Slab Lot", "Test");
        Floor sf = new Floor("SF-G", "LOT-SLAB", "G", 0);
        ParkingSlot s1 = new ParkingSlot("SL-1", "SF-G", "LOT-SLAB", "S1",
                EnumSet.of(VehicleType.CAR), false);
        sf.addSlot(s1);
        slabLot.addFloor(sf);
        lotRepo.save(slabLot);
        slotRepo.save(s1);

        ParkingService slabService = new ParkingService(
                lotRepo, slotRepo, ticketRepo, reservationRepo,
                new NearestEntryStrategy(), SlabPricingPolicy.defaultSlabs(),
                paymentService, reservationService, validator);

        ParkingTicket ticket = slabService.park(new ParkRequest("LOT-SLAB", "KA02SLAB01", VehicleType.CAR, "GATE-S"));
        System.out.println("  Slab pricing estimate (immediate): " + slabService.estimateFare(ticket.getTicketId()));

        ExitSummary exit = slabService.unpark(
                new UnparkRequest(ticket.getTicketId(), "GATE-EXIT-S", PaymentMethod.WALLET));
        System.out.println("✓ Slab exit: " + exit);
    }

    private static void demoLostTicket(ParkingService ps, String lotId) {
        ParkRequest req = new ParkRequest(lotId, "KA99LOST01", VehicleType.BIKE, "GATE-C");
        ParkingTicket ticket = ps.park(req);
        System.out.println("  Parked bike: " + ticket.getTicketId());

        ps.reportLostTicket(ticket.getTicketId());
        System.out.println("✓ Lost ticket reported. Status=" +
                ps.getTicket(ticket.getTicketId()).map(t -> t.getStatus().name()).orElse("unknown"));
    }
}

