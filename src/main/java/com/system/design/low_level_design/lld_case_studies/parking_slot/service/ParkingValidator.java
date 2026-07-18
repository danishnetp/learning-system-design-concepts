package com.system.design.low_level_design.lld_case_studies.parking_slot.service;

import com.system.design.low_level_design.lld_case_studies.parking_slot.entities.ParkingLot;
import com.system.design.low_level_design.lld_case_studies.parking_slot.exceptions.InvalidRequestException;
import com.system.design.low_level_design.lld_case_studies.parking_slot.models.ParkRequest;
import com.system.design.low_level_design.lld_case_studies.parking_slot.models.UnparkRequest;

import java.util.regex.Pattern;

/**
 * Validates incoming park/unpark requests before processing.
 */
public class ParkingValidator {

    private static final Pattern LICENSE_PLATE_PATTERN = Pattern.compile("^[A-Z0-9]{4,10}$");

    public void validateParkRequest(ParkRequest request, ParkingLot lot) {
        if (request == null) {
            throw new InvalidRequestException("ParkRequest cannot be null");
        }
        if (request.getLotId() == null || request.getLotId().isBlank()) {
            throw new InvalidRequestException("Lot ID is required");
        }
        if (request.getLicensePlate() == null || request.getLicensePlate().isBlank()) {
            throw new InvalidRequestException("License plate is required");
        }
        if (!LICENSE_PLATE_PATTERN.matcher(request.getLicensePlate().toUpperCase()).matches()) {
            throw new InvalidRequestException("Invalid license plate format: " + request.getLicensePlate());
        }
        if (request.getVehicleType() == null) {
            throw new InvalidRequestException("Vehicle type is required");
        }
        if (!lot.isActive()) {
            throw new InvalidRequestException("Lot is not accepting vehicles: " + request.getLotId());
        }
        if (lot.totalAvailableSlots() == 0) {
            throw new InvalidRequestException("Lot is full: " + request.getLotId());
        }
    }

    public void validateUnparkRequest(UnparkRequest request) {
        if (request == null) {
            throw new InvalidRequestException("UnparkRequest cannot be null");
        }
        if (request.getTicketId() == null || request.getTicketId().isBlank()) {
            throw new InvalidRequestException("Ticket ID is required");
        }
        if (request.getPaymentMethod() == null) {
            throw new InvalidRequestException("Payment method is required");
        }
    }

    /**
     * Normalizes and returns a canonical license plate string.
     */
    public String normalizePlate(String plate) {
        return plate == null ? null : plate.trim().toUpperCase().replaceAll("\\s+", "");
    }
}

