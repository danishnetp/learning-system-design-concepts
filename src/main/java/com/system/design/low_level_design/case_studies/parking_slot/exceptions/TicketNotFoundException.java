package com.system.design.low_level_design.case_studies.parking_slot.exceptions;

/** Thrown when a ticket is not found or is in an invalid state. */
public class TicketNotFoundException extends ParkingException {
    public TicketNotFoundException(String message)                  { super(message); }
    public TicketNotFoundException(String message, Throwable cause) { super(message, cause); }
}

