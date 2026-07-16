package com.system.design.low_level_design.case_studies.notification.repositories.impl;

import com.system.design.low_level_design.case_studies.notification.entities.UserPreference;
import com.system.design.low_level_design.case_studies.notification.enums.NotificationChannel;
import com.system.design.low_level_design.case_studies.notification.enums.NotificationType;
import com.system.design.low_level_design.case_studies.notification.repositories.PreferenceRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * In-memory implementation of PreferenceRepository for demonstration.
 */
public class InMemoryPreferenceRepository implements PreferenceRepository {

    private final Map<String, UserPreference> storage = new HashMap<>();

    @Override
    public UserPreference save(UserPreference preference) {
        String key = generateKey(preference.getUserId(), preference.getType(), preference.getChannel());
        storage.put(key, preference);
        return preference;
    }

    @Override
    public Optional<UserPreference> findByUserIdAndTypeAndChannel(String userId, NotificationType type, NotificationChannel channel) {
        String key = generateKey(userId, type, channel);
        return Optional.ofNullable(storage.get(key));
    }

    @Override
    public List<UserPreference> findByUserId(String userId) {
        return storage.values().stream()
                .filter(p -> p.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public UserPreference update(UserPreference preference) {
        String key = generateKey(preference.getUserId(), preference.getType(), preference.getChannel());
        storage.put(key, preference);
        return preference;
    }

    @Override
    public boolean delete(String userId, NotificationType type, NotificationChannel channel) {
        String key = generateKey(userId, type, channel);
        return storage.remove(key) != null;
    }

    private String generateKey(String userId, NotificationType type, NotificationChannel channel) {
        return userId + ":" + type + ":" + channel;
    }
}

