package com.system.design.low_level_design.case_studies.notification.service;

import com.system.design.low_level_design.case_studies.notification.entities.UserPreference;
import com.system.design.low_level_design.case_studies.notification.enums.NotificationChannel;
import com.system.design.low_level_design.case_studies.notification.enums.NotificationType;
import com.system.design.low_level_design.case_studies.notification.repositories.PreferenceRepository;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing user notification preferences.
 */
public class PreferenceService {

    private final PreferenceRepository preferenceRepository;

    public PreferenceService(PreferenceRepository preferenceRepository) {
        this.preferenceRepository = preferenceRepository;
    }

    /**
     * Checks if a notification is allowed based on user preferences.
     *
     * @param userId the user ID
     * @param type the notification type
     * @param channel the notification channel
     * @return true if notification is allowed, false if suppressed
     */
    public boolean isNotificationAllowed(String userId, NotificationType type, NotificationChannel channel) {
        Optional<UserPreference> preference = preferenceRepository.findByUserIdAndTypeAndChannel(userId, type, channel);

        if (preference.isEmpty()) {
            // Default to enabled if no preference found
            return true;
        }

        UserPreference pref = preference.get();

        // Check if disabled
        if (!pref.isEnabled()) {
            return false;
        }

        // Check if in quiet hours
        if (pref.isInQuietHours()) {
            return false;
        }

        return true;
    }

    /**
     * Gets a user preference.
     *
     * @param userId the user ID
     * @param type the notification type
     * @param channel the notification channel
     * @return the preference if found
     */
    public Optional<UserPreference> getPreference(String userId, NotificationType type, NotificationChannel channel) {
        return preferenceRepository.findByUserIdAndTypeAndChannel(userId, type, channel);
    }

    /**
     * Gets all preferences for a user.
     *
     * @param userId the user ID
     * @return list of user preferences
     */
    public List<UserPreference> getUserPreferences(String userId) {
        return preferenceRepository.findByUserId(userId);
    }

    /**
     * Saves a user preference.
     *
     * @param preference the preference to save
     * @return the saved preference
     */
    public UserPreference savePreference(UserPreference preference) {
        return preferenceRepository.save(preference);
    }

    /**
     * Updates a user preference.
     *
     * @param preference the preference to update
     * @return the updated preference
     */
    public UserPreference updatePreference(UserPreference preference) {
        return preferenceRepository.update(preference);
    }

    /**
     * Deletes a user preference.
     *
     * @param userId the user ID
     * @param type the notification type
     * @param channel the notification channel
     * @return true if deleted, false if not found
     */
    public boolean deletePreference(String userId, NotificationType type, NotificationChannel channel) {
        return preferenceRepository.delete(userId, type, channel);
    }
}

