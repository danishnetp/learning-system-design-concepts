package com.system.design.low_level_design.lld_case_studies.parking_slot.models;

import com.system.design.low_level_design.lld_case_studies.parking_slot.enums.VehicleType;

/**
 * Input DTO for parking entry.
 */
public class ParkRequest {

    private final String lotId;
    private final String licensePlate;
    private final VehicleType vehicleType;
    private final String entryGateId;
    private final String idempotencyKey;
    private final boolean requiresCharger;
    private final String reservationId; // null for walk-in

    public ParkRequest(String lotId, String licensePlate, VehicleType vehicleType,
                       String entryGateId, String idempotencyKey,
                       boolean requiresCharger, String reservationId) {
        this.lotId = lotId;
        this.licensePlate = licensePlate;
        this.vehicleType = vehicleType;
        this.entryGateId = entryGateId;
        this.idempotencyKey = idempotencyKey;
        this.requiresCharger = requiresCharger;
        this.reservationId = reservationId;
    }

    // Convenience constructor for walk-in without charger
    public ParkRequest(String lotId, String licensePlate, VehicleType vehicleType, String entryGateId) {
        this(lotId, licensePlate, vehicleType, entryGateId, licensePlate + "-" + System.currentTimeMillis(), false, null);
    }

    public String getLotId()          { return lotId; }
    public String getLicensePlate()   { return licensePlate; }
    public VehicleType getVehicleType() { return vehicleType; }
    public String getEntryGateId()    { return entryGateId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public boolean isRequiresCharger() { return requiresCharger; }
    public String getReservationId()  { return reservationId; }
    public boolean isReservation()    { return reservationId != null && !reservationId.isBlank(); }
}

