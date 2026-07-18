package com.system.design.low_level_design.lld_case_studies.notification.interfaces;

import com.system.design.low_level_design.lld_case_studies.notification.entities.Notification;
import com.system.design.low_level_design.lld_case_studies.notification.enums.NotificationChannel;
import com.system.design.low_level_design.lld_case_studies.notification.models.SendResult;

/**
 * Interface for sending notifications through a specific channel.
 */
public interface NotificationSender {

    /**
     * Sends a notification through the channel.
     *
     * @param notification the notification to send
     * @return send result with status and metadata
     */
    SendResult send(Notification notification);

    /**
     * Returns the channel this sender handles.
     *
     * @return notification channel
     */
    NotificationChannel getChannel();

    /**
     * Validates if the notification is ready to be sent.
     *
     * @param notification the notification to validate
     * @return true if valid, false otherwise
     */
    boolean canSend(Notification notification);
}

