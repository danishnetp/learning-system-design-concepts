package com.system.design.low_level_design.lld_case_studies.parking_slot.interfaces;

import com.system.design.low_level_design.lld_case_studies.parking_slot.models.Money;
import com.system.design.low_level_design.lld_case_studies.parking_slot.models.TicketSnapshot;

/**
 * Strategy for computing parking fare.
 * Implementations vary by policy (flat-rate, slab, dynamic, etc.).
 */
public interface PricingPolicy {

    /**
     * Computes the fare for a completed/ongoing parking session.
     *
     * @param snapshot immutable ticket snapshot at exit time
     * @return computed amount to charge
     */
    Money calculate(TicketSnapshot snapshot);

    /**
     * Returns the policy version identifier (stored in ticket for audit).
     */
    String policyVersion();
}

