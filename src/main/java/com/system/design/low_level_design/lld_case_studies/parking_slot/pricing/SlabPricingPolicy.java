package com.system.design.low_level_design.lld_case_studies.parking_slot.pricing;

import com.system.design.low_level_design.lld_case_studies.parking_slot.interfaces.PricingPolicy;
import com.system.design.low_level_design.lld_case_studies.parking_slot.models.Money;
import com.system.design.low_level_design.lld_case_studies.parking_slot.models.TicketSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Slab-based pricing: different rates apply for different duration ranges.
 * Example: 0-2h = ₹30, 2-6h = ₹60, 6h+ = ₹100.
 * Slabs are accumulated rather than selecting a single slab.
 */
public class SlabPricingPolicy implements PricingPolicy {

    /**
     * Defines a pricing slab: charge {@code rate} for minutes within [{@code from}, {@code to}).
     * {@code to == Long.MAX_VALUE} means open-ended (remaining duration).
     */
    public static class Slab {
        private final long fromMinute;
        private final long toMinute;
        private final Money rate; // per hour for this slab

        public Slab(long fromMinute, long toMinute, Money rate) {
            this.fromMinute = fromMinute;
            this.toMinute = toMinute;
            this.rate = rate;
        }
    }

    private final List<Slab> slabs;

    public SlabPricingPolicy(List<Slab> slabs) {
        this.slabs = new ArrayList<>(slabs);
    }

    /**
     * Creates a common 3-slab config: 0-2h ₹30/h, 2-6h ₹50/h, 6h+ ₹80/h.
     */
    public static SlabPricingPolicy defaultSlabs() {
        List<Slab> slabs = List.of(
                new Slab(0, 120, new Money(30)),
                new Slab(120, 360, new Money(50)),
                new Slab(360, Long.MAX_VALUE, new Money(80))
        );
        return new SlabPricingPolicy(slabs);
    }

    @Override
    public Money calculate(TicketSnapshot snapshot) {
        long totalMinutes = snapshot.durationMinutes();
        Money total = Money.ZERO;

        for (Slab slab : slabs) {
            if (totalMinutes <= slab.fromMinute) break;

            long slabEnd = Math.min(totalMinutes, slab.toMinute);
            long minutesInSlab = slabEnd - slab.fromMinute;

            // Ceiling division: each started hour within slab is billed
            long hoursInSlab = (minutesInSlab + 59) / 60;
            total = total.add(slab.rate.multiply(hoursInSlab));
        }

        return total;
    }

    @Override
    public String policyVersion() { return "SLAB_V1"; }
}

