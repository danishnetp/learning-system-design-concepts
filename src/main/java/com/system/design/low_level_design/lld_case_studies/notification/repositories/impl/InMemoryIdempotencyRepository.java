package com.system.design.low_level_design.lld_case_studies.notification.repositories.impl;

import com.system.design.low_level_design.lld_case_studies.notification.repositories.IdempotencyRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory implementation of IdempotencyRepository for demonstration.
 * In production, use a database with TTL support (e.g., Redis).
 */
public class InMemoryIdempotencyRepository implements IdempotencyRepository {

    private static final long TTL_MILLIS = 24 * 60 * 60 * 1000; // 24 hours

    private final Map<String, IdempotencyRecord> storage = new HashMap<>();

    @Override
    public synchronized boolean tryCreate(String key) {
        if (storage.containsKey(key)) {
            IdempotencyRecord record = storage.get(key);
            // Check if record is expired
            if (isExpired(record.createdAt, System.currentTimeMillis())) {
                storage.remove(key);
                storage.put(key, new IdempotencyRecord(Instant.now(), null));
                return true;
            }
            return false;
        }
        storage.put(key, new IdempotencyRecord(Instant.now(), null));
        return true;
    }

    @Override
    public synchronized Optional<String> getExistingResult(String key) {
        if (!storage.containsKey(key)) {
            return Optional.empty();
        }
        IdempotencyRecord record = storage.get(key);
        if (isExpired(record.createdAt, System.currentTimeMillis())) {
            storage.remove(key);
            return Optional.empty();
        }
        return Optional.ofNullable(record.result);
    }

    @Override
    public synchronized void storeResult(String key, String result) {
        if (storage.containsKey(key)) {
            storage.put(key, new IdempotencyRecord(storage.get(key).createdAt, result));
        }
    }

    @Override
    public synchronized void cleanupExpired() {
        long now = System.currentTimeMillis();
        storage.entrySet().removeIf(entry -> isExpired(entry.getValue().createdAt, now));
    }

    private boolean isExpired(Instant createdAt, long nowMillis) {
        return (nowMillis - createdAt.toEpochMilli()) > TTL_MILLIS;
    }

    private static class IdempotencyRecord {
        Instant createdAt;
        String result;

        IdempotencyRecord(Instant createdAt, String result) {
            this.createdAt = createdAt;
            this.result = result;
        }
    }
}

