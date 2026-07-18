package com.system.design.low_level_design.lld_case_studies.notification.repositories.impl;

import com.system.design.low_level_design.lld_case_studies.notification.entities.Notification;
import com.system.design.low_level_design.lld_case_studies.notification.repositories.NotificationRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * In-memory implementation of NotificationRepository for demonstration.
 * In production, use a real database.
 */
public class InMemoryNotificationRepository implements NotificationRepository {

    private final Map<String, Notification> storage = new HashMap<>();

    @Override
    public Notification save(Notification notification) {
        storage.put(notification.getNotificationId(), notification);
        return notification;
    }

    @Override
    public Optional<Notification> findById(String notificationId) {
        return Optional.ofNullable(storage.get(notificationId));
    }

    @Override
    public List<Notification> findByUserId(String userId) {
        return storage.values().stream()
                .filter(n -> n.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public Notification update(Notification notification) {
        storage.put(notification.getNotificationId(), notification);
        return notification;
    }

    @Override
    public boolean delete(String notificationId) {
        return storage.remove(notificationId) != null;
    }
}

