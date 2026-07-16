package com.system.design.low_level_design.case_studies.parking_slot.exceptions;

/** Base exception for all parking system errors. */
public class ParkingException extends RuntimeException {
    public ParkingException(String message)                    { super(message); }
    public ParkingException(String message, Throwable cause)   { super(message, cause); }
}

