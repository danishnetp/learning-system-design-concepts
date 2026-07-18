package com.system.design.low_level_design.lld_case_studies.parking_slot.exceptions;

/** Thrown when no suitable slot is available. */
public class SlotUnavailableException extends ParkingException {
    public SlotUnavailableException(String message)                  { super(message); }
    public SlotUnavailableException(String message, Throwable cause) { super(message, cause); }
}

