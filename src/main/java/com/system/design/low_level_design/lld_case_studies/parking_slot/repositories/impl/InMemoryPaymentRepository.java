package com.system.design.low_level_design.lld_case_studies.parking_slot.repositories.impl;

import com.system.design.low_level_design.lld_case_studies.parking_slot.entities.Payment;
import com.system.design.low_level_design.lld_case_studies.parking_slot.repositories.PaymentRepository;

import java.util.*;

public class InMemoryPaymentRepository implements PaymentRepository {

    private final Map<String, Payment> store = new HashMap<>();

    @Override public Payment save(Payment p)   { store.put(p.getPaymentId(), p); return p; }
    @Override public Optional<Payment> findById(String id) { return Optional.ofNullable(store.get(id)); }
    @Override public Payment update(Payment p) { store.put(p.getPaymentId(), p); return p; }

    @Override
    public Optional<Payment> findByIdempotencyKey(String key) {
        return store.values().stream()
                .filter(p -> p.getIdempotencyKey().equals(key))
                .findFirst();
    }

    @Override
    public Optional<Payment> findByTicketId(String ticketId) {
        return store.values().stream()
                .filter(p -> p.getTicketId().equals(ticketId))
                .findFirst();
    }
}

