package com.system.design.low_level_design.case_studies.notification.sender;

import com.system.design.low_level_design.case_studies.notification.entities.Notification;
import com.system.design.low_level_design.case_studies.notification.entities.User;
import com.system.design.low_level_design.case_studies.notification.enums.NotificationChannel;
import com.system.design.low_level_design.case_studies.notification.interfaces.ChannelProvider;
import com.system.design.low_level_design.case_studies.notification.interfaces.NotificationSender;
import com.system.design.low_level_design.case_studies.notification.models.SendResult;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Notification sender for Push notification channel.
 */
public class PushNotificationSender implements NotificationSender {

    private static final Logger LOGGER = Logger.getLogger(PushNotificationSender.class.getName());
    private final ChannelProvider pushProvider;

    public PushNotificationSender(ChannelProvider pushProvider) {
        this.pushProvider = pushProvider;
    }

    @Override
    public SendResult send(Notification notification) {
        // Note: In production, fetch user from repository
        User user = new User(notification.getUserId(), null, null, "device-token-12345", "en_US");

        if (!canSend(notification)) {
            return SendResult.failure("Missing device token", "NO_DEVICE_TOKEN", "User device not configured", false);
        }

        try {
            Map<String, String> metadata = new HashMap<>();
            metadata.put("templateId", notification.getTemplateId());
            metadata.put("userId", notification.getUserId());
            metadata.put("priority", notification.getPriority().getDisplayName());

            SendResult result = pushProvider.deliver(
                    user.getDeviceToken(),
                    notification.getSubject(), // Title for push
                    notification.getBody(),
                    metadata
            );

            if (result.isSuccess()) {
                LOGGER.info("Push notification sent to device: " + user.getDeviceToken());
            }

            return result;
        } catch (Exception e) {
            LOGGER.severe("Error sending push: " + e.getMessage());
            return SendResult.failure("Push send error", "SEND_EXCEPTION", e.getMessage(), true);
        }
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.PUSH;
    }

    @Override
    public boolean canSend(Notification notification) {
        return notification != null && notification.getBody() != null;
    }
}

