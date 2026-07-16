package com.system.design.low_level_design.case_studies.notification.interfaces;

import com.system.design.low_level_design.case_studies.notification.entities.Notification;
import java.time.Duration;

/**
 * Interface for defining retry policies for failed notifications.
 */
public interface RetryPolicy {

    /**
     * Determines if a notification should be retried.
     *
     * @param notification the notification to check
     * @param attemptCount current attempt count
     * @return true if should retry, false otherwise
     */
    boolean canRetry(Notification notification, int attemptCount);

    /**
     * Calculates the backoff duration before the next retry.
     *
     * @param attemptCount current attempt count
     * @return duration to wait before next retry
     */
    Duration getNextBackoff(int attemptCount);

    /**
     * Returns the maximum number of retry attempts allowed.
     *
     * @return max retry attempts
     */
    int getMaxRetries();
}

