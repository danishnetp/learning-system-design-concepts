package com.system.design.low_level_design.lld_case_studies.notification.repositories;

import com.system.design.low_level_design.lld_case_studies.notification.entities.DeliveryAttempt;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for DeliveryAttempt persistence.
 */
public interface DeliveryAttemptRepository {

    /**
     * Saves a delivery attempt.
     *
     * @param attempt the attempt to save
     * @return saved attempt
     */
    DeliveryAttempt save(DeliveryAttempt attempt);

    /**
     * Finds an attempt by ID.
     *
     * @param attemptId the attempt ID
     * @return optional containing the attempt if found
     */
    Optional<DeliveryAttempt> findById(String attemptId);

    /**
     * Finds all attempts for a notification.
     *
     * @param notificationId the notification ID
     * @return list of attempts
     */
    List<DeliveryAttempt> findByNotificationId(String notificationId);

    /**
     * Updates an attempt.
     *
     * @param attempt the attempt to update
     * @return updated attempt
     */
    DeliveryAttempt update(DeliveryAttempt attempt);
}

