package com.system.design.low_level_design.case_studies.parking_slot.exceptions;

/** Thrown when validation on an incoming request fails. */
public class InvalidRequestException extends ParkingException {
    public InvalidRequestException(String message)                  { super(message); }
    public InvalidRequestException(String message, Throwable cause) { super(message, cause); }
}

