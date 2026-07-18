package com.system.design.low_level_design.lld_case_studies.parking_slot.models;

import com.system.design.low_level_design.lld_case_studies.parking_slot.entities.ParkingTicket;

import java.time.Instant;

/**
 * Immutable snapshot of ticket data used by the pricing engine.
 * Decouples pricing logic from the live ticket entity.
 */
public class TicketSnapshot {

    private final String ticketId;
    private final String vehiclePlate;
    private final String lotId;
    private final String slotId;
    private final Instant entryTime;
    private final Instant exitTime;
    private final String pricingVersion;

    public TicketSnapshot(ParkingTicket ticket, Instant exitTime) {
        this.ticketId      = ticket.getTicketId();
        this.vehiclePlate  = ticket.getVehiclePlate();
        this.lotId         = ticket.getLotId();
        this.slotId        = ticket.getSlotId();
        this.entryTime     = ticket.getEntryTime();
        this.exitTime      = exitTime;
        this.pricingVersion = ticket.getPricingVersion();
    }

    public String getTicketId()       { return ticketId; }
    public String getVehiclePlate()   { return vehiclePlate; }
    public String getLotId()          { return lotId; }
    public String getSlotId()         { return slotId; }
    public Instant getEntryTime()     { return entryTime; }
    public Instant getExitTime()      { return exitTime; }
    public String getPricingVersion() { return pricingVersion; }

    public long durationMinutes() {
        return java.time.Duration.between(entryTime, exitTime).toMinutes();
    }
}

