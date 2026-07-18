package com.system.design.low_level_design.lld_case_studies.notification.repositories;

import com.system.design.low_level_design.lld_case_studies.notification.entities.Notification;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Notification persistence.
 */
public interface NotificationRepository {

    /**
     * Saves a notification.
     *
     * @param notification the notification to save
     * @return saved notification
     */
    Notification save(Notification notification);

    /**
     * Finds a notification by ID.
     *
     * @param notificationId the notification ID
     * @return optional containing the notification if found
     */
    Optional<Notification> findById(String notificationId);

    /**
     * Finds all notifications for a user.
     *
     * @param userId the user ID
     * @return list of notifications
     */
    List<Notification> findByUserId(String userId);

    /**
     * Updates a notification.
     *
     * @param notification the notification to update
     * @return updated notification
     */
    Notification update(Notification notification);

    /**
     * Deletes a notification.
     *
     * @param notificationId the notification ID
     * @return true if deleted, false if not found
     */
    boolean delete(String notificationId);
}

