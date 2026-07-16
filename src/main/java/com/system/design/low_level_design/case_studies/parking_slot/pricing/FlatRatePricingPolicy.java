package com.system.design.low_level_design.case_studies.parking_slot.pricing;

import com.system.design.low_level_design.case_studies.parking_slot.interfaces.PricingPolicy;
import com.system.design.low_level_design.case_studies.parking_slot.models.Money;
import com.system.design.low_level_design.case_studies.parking_slot.models.TicketSnapshot;

/**
 * Flat-rate pricing: a fixed amount per started hour.
 * Grace period of 10 minutes applied before billing starts.
 */
public class FlatRatePricingPolicy implements PricingPolicy {

    private static final long GRACE_PERIOD_MINUTES = 10;
    private final Money ratePerHour;

    public FlatRatePricingPolicy(Money ratePerHour) {
        this.ratePerHour = ratePerHour;
    }

    @Override
    public Money calculate(TicketSnapshot snapshot) {
        long minutes = snapshot.durationMinutes();

        // Grace period
        if (minutes <= GRACE_PERIOD_MINUTES) {
            return Money.ZERO;
        }

        // Ceiling division: each started hour is billed fully
        long billableMinutes = minutes - GRACE_PERIOD_MINUTES;
        long hoursCharged = (billableMinutes + 59) / 60;

        return ratePerHour.multiply(hoursCharged);
    }

    @Override
    public String policyVersion() { return "FLAT_RATE_V1"; }
}

