package com.system.design.low_level_design.case_studies.notification.sender;

import com.system.design.low_level_design.case_studies.notification.entities.Notification;
import com.system.design.low_level_design.case_studies.notification.enums.NotificationChannel;
import com.system.design.low_level_design.case_studies.notification.interfaces.NotificationSender;
import com.system.design.low_level_design.case_studies.notification.models.SendResult;
import java.util.logging.Logger;

/**
 * Notification sender for In-App notification channel.
 */
public class InAppNotificationSender implements NotificationSender {

    private static final Logger LOGGER = Logger.getLogger(InAppNotificationSender.class.getName());

    public InAppNotificationSender() {
    }

    @Override
    public SendResult send(Notification notification) {
        if (!canSend(notification)) {
            return SendResult.failure("Missing notification content", "NO_CONTENT", "Notification content required", false);
        }

        try {
            // In-app notifications are typically stored in database and delivered via WebSocket or polling
            // For this implementation, we'll treat storage as success
            LOGGER.info("In-app notification stored for user: " + notification.getUserId());

            return SendResult.delivered("In-app notification stored successfully");
        } catch (Exception e) {
            LOGGER.severe("Error storing in-app notification: " + e.getMessage());
            return SendResult.failure("In-app storage error", "STORAGE_EXCEPTION", e.getMessage(), false);
        }
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.IN_APP;
    }

    @Override
    public boolean canSend(Notification notification) {
        return notification != null &&
                notification.getBody() != null &&
                !notification.getBody().isEmpty();
    }
}

