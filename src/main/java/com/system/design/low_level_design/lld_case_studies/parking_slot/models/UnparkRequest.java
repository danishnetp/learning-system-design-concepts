package com.system.design.low_level_design.lld_case_studies.parking_slot.models;

import com.system.design.low_level_design.lld_case_studies.parking_slot.enums.PaymentMethod;

/**
 * Input DTO for parking exit / checkout.
 */
public class UnparkRequest {

    private final String ticketId;
    private final String exitGateId;
    private final PaymentMethod paymentMethod;
    private final String idempotencyKey;

    public UnparkRequest(String ticketId, String exitGateId, PaymentMethod paymentMethod, String idempotencyKey) {
        this.ticketId = ticketId;
        this.exitGateId = exitGateId;
        this.paymentMethod = paymentMethod;
        this.idempotencyKey = idempotencyKey;
    }

    public UnparkRequest(String ticketId, String exitGateId, PaymentMethod paymentMethod) {
        this(ticketId, exitGateId, paymentMethod, ticketId + "-exit");
    }

    public String getTicketId()       { return ticketId; }
    public String getExitGateId()     { return exitGateId; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public String getIdempotencyKey() { return idempotencyKey; }
}

