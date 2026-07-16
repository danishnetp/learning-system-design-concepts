package com.system.design.low_level_design.case_studies.parking_slot.models;

import com.system.design.low_level_design.case_studies.parking_slot.enums.VehicleType;

import java.time.Instant;

/**
 * Input DTO for creating a reservation.
 */
public class CreateReservationRequest {

    private final String userId;
    private final String lotId;
    private final VehicleType vehicleType;
    private final Instant reservedFrom;
    private final Instant reservedTo;
    private final boolean requiresCharger;

    public CreateReservationRequest(String userId, String lotId, VehicleType vehicleType,
                                    Instant reservedFrom, Instant reservedTo, boolean requiresCharger) {
        this.userId = userId;
        this.lotId = lotId;
        this.vehicleType = vehicleType;
        this.reservedFrom = reservedFrom;
        this.reservedTo = reservedTo;
        this.requiresCharger = requiresCharger;
    }

    public String getUserId()          { return userId; }
    public String getLotId()           { return lotId; }
    public VehicleType getVehicleType() { return vehicleType; }
    public Instant getReservedFrom()   { return reservedFrom; }
    public Instant getReservedTo()     { return reservedTo; }
    public boolean isRequiresCharger() { return requiresCharger; }
}

