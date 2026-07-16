package com.system.design.low_level_design.case_studies.notification.repositories.impl;

import com.system.design.low_level_design.case_studies.notification.entities.DeliveryAttempt;
import com.system.design.low_level_design.case_studies.notification.repositories.DeliveryAttemptRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * In-memory implementation of DeliveryAttemptRepository for demonstration.
 */
public class InMemoryDeliveryAttemptRepository implements DeliveryAttemptRepository {

    private final Map<String, DeliveryAttempt> storage = new HashMap<>();

    @Override
    public DeliveryAttempt save(DeliveryAttempt attempt) {
        storage.put(attempt.getAttemptId(), attempt);
        return attempt;
    }

    @Override
    public Optional<DeliveryAttempt> findById(String attemptId) {
        return Optional.ofNullable(storage.get(attemptId));
    }

    @Override
    public List<DeliveryAttempt> findByNotificationId(String notificationId) {
        return storage.values().stream()
                .filter(a -> a.getNotificationId().equals(notificationId))
                .collect(Collectors.toList());
    }

    @Override
    public DeliveryAttempt update(DeliveryAttempt attempt) {
        storage.put(attempt.getAttemptId(), attempt);
        return attempt;
    }
}

