package com.system.design.low_level_design.case_studies.notification.repositories;

import com.system.design.low_level_design.case_studies.notification.entities.UserPreference;
import com.system.design.low_level_design.case_studies.notification.enums.NotificationChannel;
import com.system.design.low_level_design.case_studies.notification.enums.NotificationType;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for UserPreference persistence.
 */
public interface PreferenceRepository {

    /**
     * Saves a user preference.
     *
     * @param preference the preference to save
     * @return saved preference
     */
    UserPreference save(UserPreference preference);

    /**
     * Finds a user preference by user, type, and channel.
     *
     * @param userId the user ID
     * @param type the notification type
     * @param channel the notification channel
     * @return optional containing the preference if found
     */
    Optional<UserPreference> findByUserIdAndTypeAndChannel(String userId, NotificationType type, NotificationChannel channel);

    /**
     * Finds all preferences for a user.
     *
     * @param userId the user ID
     * @return list of preferences
     */
    List<UserPreference> findByUserId(String userId);

    /**
     * Updates a preference.
     *
     * @param preference the preference to update
     * @return updated preference
     */
    UserPreference update(UserPreference preference);

    /**
     * Deletes a preference.
     *
     * @param userId the user ID
     * @param type the notification type
     * @param channel the notification channel
     * @return true if deleted, false if not found
     */
    boolean delete(String userId, NotificationType type, NotificationChannel channel);
}

