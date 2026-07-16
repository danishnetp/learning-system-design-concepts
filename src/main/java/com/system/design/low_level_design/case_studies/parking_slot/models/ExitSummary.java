package com.system.design.low_level_design.case_studies.parking_slot.models;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

/**
 * Result returned after successful checkout.
 */
public class ExitSummary {

    private final String ticketId;
    private final String slotId;
    private final String licensePlate;
    private final Instant entryTime;
    private final Instant exitTime;
    private final Duration duration;
    private final BigDecimal amountCharged;
    private final String paymentId;
    private final String message;

    public ExitSummary(String ticketId, String slotId, String licensePlate,
                       Instant entryTime, Instant exitTime, BigDecimal amountCharged,
                       String paymentId, String message) {
        this.ticketId = ticketId;
        this.slotId = slotId;
        this.licensePlate = licensePlate;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.duration = Duration.between(entryTime, exitTime);
        this.amountCharged = amountCharged;
        this.paymentId = paymentId;
        this.message = message;
    }

    public String getTicketId()        { return ticketId; }
    public String getSlotId()          { return slotId; }
    public String getLicensePlate()    { return licensePlate; }
    public Instant getEntryTime()      { return entryTime; }
    public Instant getExitTime()       { return exitTime; }
    public Duration getDuration()      { return duration; }
    public BigDecimal getAmountCharged() { return amountCharged; }
    public String getPaymentId()       { return paymentId; }
    public String getMessage()         { return message; }

    @Override
    public String toString() {
        long mins = duration.toMinutes();
        return "ExitSummary{ticket='" + ticketId +
                "', plate='" + licensePlate +
                "', duration=" + mins + "m" +
                ", amount=₹" + amountCharged +
                ", paymentId='" + paymentId + "'}";
    }
}

