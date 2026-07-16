package com.system.design.low_level_design.case_studies.parking_slot.entities;

import com.system.design.low_level_design.case_studies.parking_slot.enums.ReservationStatus;
import com.system.design.low_level_design.case_studies.parking_slot.enums.VehicleType;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a pre-booked parking slot reservation.
 */
public class Reservation {

    private final String reservationId;
    private final String userId;
    private final String lotId;
    private final VehicleType vehicleType;
    private final Instant reservedFrom;
    private final Instant reservedTo;
    private final Instant createdAt;

    private ReservationStatus status;
    private String assignedSlotId;
    private String checkedInTicketId;

    public Reservation(String userId, String lotId, VehicleType vehicleType,
                       Instant reservedFrom, Instant reservedTo) {
        this.reservationId = UUID.randomUUID().toString();
        this.userId = userId;
        this.lotId = lotId;
        this.vehicleType = vehicleType;
        this.reservedFrom = reservedFrom;
        this.reservedTo = reservedTo;
        this.status = ReservationStatus.CREATED;
        this.createdAt = Instant.now();
    }

    // State transitions
    public void confirm(String slotId) {
        this.assignedSlotId = slotId;
        this.status = ReservationStatus.CONFIRMED;
    }

    public void checkIn(String ticketId) {
        this.checkedInTicketId = ticketId;
        this.status = ReservationStatus.CHECKED_IN;
    }

    public void expire()    { this.status = ReservationStatus.EXPIRED; }
    public void cancel()    { this.status = ReservationStatus.CANCELLED; }
    public void markNoShow() { this.status = ReservationStatus.NO_SHOW; }

    public boolean isHoldWindowActive() {
        // Consider a 15-minute hold window after reservedFrom
        return Instant.now().isBefore(reservedFrom.plusSeconds(15 * 60));
    }

    // Getters
    public String getReservationId()   { return reservationId; }
    public String getUserId()          { return userId; }
    public String getLotId()           { return lotId; }
    public VehicleType getVehicleType() { return vehicleType; }
    public Instant getReservedFrom()   { return reservedFrom; }
    public Instant getReservedTo()     { return reservedTo; }
    public ReservationStatus getStatus() { return status; }
    public String getAssignedSlotId()  { return assignedSlotId; }
    public String getCheckedInTicketId() { return checkedInTicketId; }
    public Instant getCreatedAt()      { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation)) return false;
        return Objects.equals(reservationId, ((Reservation) o).reservationId);
    }

    @Override
    public int hashCode() { return Objects.hash(reservationId); }

    @Override
    public String toString() {
        return "Reservation{id='" + reservationId + "', user='" + userId +
                "', status=" + status + ", slot='" + assignedSlotId + "'}";
    }
}

