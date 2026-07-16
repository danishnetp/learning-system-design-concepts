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
 * Notification sender for SMS channel.
 */
public class SmsNotificationSender implements NotificationSender {

    private static final Logger LOGGER = Logger.getLogger(SmsNotificationSender.class.getName());
    private final ChannelProvider smsProvider;

    public SmsNotificationSender(ChannelProvider smsProvider) {
        this.smsProvider = smsProvider;
    }

    @Override
    public SendResult send(Notification notification) {
        // Note: In production, fetch user from repository
        User user = new User(notification.getUserId(), null, "+1234567890", null, "en_US");

        if (!canSend(notification)) {
            return SendResult.failure("Missing phone number", "NO_PHONE", "User phone not configured", false);
        }

        try {
            // SMS has character limit, so use body only
            String smsBody = notification.getBody();
            if (smsBody.length() > 160) {
                smsBody = smsBody.substring(0, 157) + "...";
            }

            Map<String, String> metadata = new HashMap<>();
            metadata.put("templateId", notification.getTemplateId());
            metadata.put("userId", notification.getUserId());

            SendResult result = smsProvider.deliver(
                    user.getPhoneNumber(),
                    null, // SMS doesn't use subject
                    smsBody,
                    metadata
            );

            if (result.isSuccess()) {
                LOGGER.info("SMS sent successfully to: " + user.getPhoneNumber());
            }

            return result;
        } catch (Exception e) {
            LOGGER.severe("Error sending SMS: " + e.getMessage());
            return SendResult.failure("SMS send error", "SEND_EXCEPTION", e.getMessage(), true);
        }
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }

    @Override
    public boolean canSend(Notification notification) {
        return notification != null && notification.getBody() != null;
    }
}

