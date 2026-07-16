package com.system.design.low_level_design.case_studies.notification.repositories;

import java.util.Optional;

/**
 * Repository interface for idempotency tracking.
 */
public interface IdempotencyRepository {

    /**
     * Tries to create an idempotency record for a key.
     * Returns true if successfully created (first time), false if already exists.
     *
     * @param key the idempotency key
     * @return true if successfully created, false if already exists
     */
    boolean tryCreate(String key);

    /**
     * Gets an existing idempotency record result.
     *
     * @param key the idempotency key
     * @return optional containing the result if found
     */
    Optional<String> getExistingResult(String key);

    /**
     * Stores result for an idempotency key.
     *
     * @param key the idempotency key
     * @param result the result to store
     */
    void storeResult(String key, String result);

    /**
     * Cleans up expired idempotency records (older than TTL).
     */
    void cleanupExpired();
}

