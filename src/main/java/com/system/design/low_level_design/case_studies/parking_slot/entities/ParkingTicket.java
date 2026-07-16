package com.system.design.low_level_design.case_studies.parking_slot.entities;

import com.system.design.low_level_design.case_studies.parking_slot.enums.TicketStatus;
import com.system.design.low_level_design.case_studies.parking_slot.enums.VehicleType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an active parking session.
 * Immutable entry details; mutable lifecycle/payment state.
 */
public class ParkingTicket {

    // Immutable entry details
    private final String ticketId;
    private final String lotId;
    private final String slotId;
    private final String vehiclePlate;
    private final VehicleType vehicleType;
    private final String entryGateId;
    private final Instant entryTime;
    private final String pricingVersion;

    // Mutable lifecycle state
    private TicketStatus status;
    private Instant exitTime;
    private BigDecimal totalAmount;
    private String paymentId;

    public ParkingTicket(String lotId, String slotId, String vehiclePlate,
                         VehicleType vehicleType, String entryGateId, String pricingVersion) {
        this.ticketId = UUID.randomUUID().toString();
        this.lotId = lotId;
        this.slotId = slotId;
        this.vehiclePlate = vehiclePlate;
        this.vehicleType = vehicleType;
        this.entryGateId = entryGateId;
        this.pricingVersion = pricingVersion;
        this.entryTime = Instant.now();
        this.status = TicketStatus.ACTIVE;
    }

    // Getters - immutable fields
    public String getTicketId()      { return ticketId; }
    public String getLotId()         { return lotId; }
    public String getSlotId()        { return slotId; }
    public String getVehiclePlate()  { return vehiclePlate; }
    public VehicleType getVehicleType() { return vehicleType; }
    public String getEntryGateId()   { return entryGateId; }
    public Instant getEntryTime()    { return entryTime; }
    public String getPricingVersion() { return pricingVersion; }

    // Getters - mutable fields
    public TicketStatus getStatus()  { return status; }
    public Instant getExitTime()     { return exitTime; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getPaymentId()     { return paymentId; }

    // Setters - lifecycle transitions only
    public void markPaymentPending(BigDecimal amount) {
        this.totalAmount = amount;
        this.status = TicketStatus.PAYMENT_PENDING;
        this.exitTime = Instant.now();
    }

    public void markPaid(String paymentId) {
        this.paymentId = paymentId;
        this.status = TicketStatus.PAID;
    }

    public void markClosed() {
        this.status = TicketStatus.CLOSED;
    }

    public void markLost() {
        this.status = TicketStatus.LOST;
    }

    public void markCancelled() {
        this.status = TicketStatus.CANCELLED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParkingTicket)) return false;
        return Objects.equals(ticketId, ((ParkingTicket) o).ticketId);
    }

    @Override
    public int hashCode() { return Objects.hash(ticketId); }

    @Override
    public String toString() {
        return "ParkingTicket{ticketId='" + ticketId + "', plate='" + vehiclePlate +
                "', slot='" + slotId + "', status=" + status + "}";
    }
}

