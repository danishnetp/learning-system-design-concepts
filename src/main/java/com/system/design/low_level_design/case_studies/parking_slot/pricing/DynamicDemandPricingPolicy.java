package com.system.design.low_level_design.case_studies.parking_slot.pricing;

import com.system.design.low_level_design.case_studies.parking_slot.interfaces.PricingPolicy;
import com.system.design.low_level_design.case_studies.parking_slot.models.Money;
import com.system.design.low_level_design.case_studies.parking_slot.models.TicketSnapshot;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Dynamic pricing based on time-of-day and day-of-week.
 * Applies a multiplier on top of a base rate during peak hours and weekends.
 */
public class DynamicDemandPricingPolicy implements PricingPolicy {

    private final Money baseRatePerHour;
    private final double peakMultiplier;   // e.g., 1.5
    private final double weekendMultiplier; // e.g., 1.2

    private static final int PEAK_START_HOUR = 8;
    private static final int PEAK_END_HOUR   = 20;

    public DynamicDemandPricingPolicy(Money baseRatePerHour, double peakMultiplier, double weekendMultiplier) {
        this.baseRatePerHour    = baseRatePerHour;
        this.peakMultiplier     = peakMultiplier;
        this.weekendMultiplier  = weekendMultiplier;
    }

    @Override
    public Money calculate(TicketSnapshot snapshot) {
        long totalMinutes = snapshot.durationMinutes();
        if (totalMinutes == 0) return Money.ZERO;

        long hoursCharged = (totalMinutes + 59) / 60;
        double effectiveMultiplier = computeMultiplier(snapshot);
        return baseRatePerHour.multiply(hoursCharged).multiply(effectiveMultiplier);
    }

    private double computeMultiplier(TicketSnapshot snapshot) {
        LocalDateTime exitLocal = LocalDateTime.ofInstant(snapshot.getExitTime(), ZoneId.systemDefault());
        int hour = exitLocal.getHour();
        DayOfWeek day = exitLocal.getDayOfWeek();

        double multiplier = 1.0;
        if (hour >= PEAK_START_HOUR && hour < PEAK_END_HOUR) {
            multiplier = Math.max(multiplier, peakMultiplier);
        }
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            multiplier = Math.max(multiplier, weekendMultiplier);
        }
        return multiplier;
    }

    @Override
    public String policyVersion() { return "DYNAMIC_V1"; }
}

