package com.system.design.low_level_design.lld_case_studies.notification.sender;

import com.system.design.low_level_design.lld_case_studies.notification.entities.Notification;
import com.system.design.low_level_design.lld_case_studies.notification.entities.User;
import com.system.design.low_level_design.lld_case_studies.notification.enums.NotificationChannel;
import com.system.design.low_level_design.lld_case_studies.notification.interfaces.ChannelProvider;
import com.system.design.low_level_design.lld_case_studies.notification.interfaces.NotificationSender;
import com.system.design.low_level_design.lld_case_studies.notification.models.SendResult;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Notification sender for Email channel.
 */
public class EmailNotificationSender implements NotificationSender {

    private static final Logger LOGGER = Logger.getLogger(EmailNotificationSender.class.getName());
    private final ChannelProvider emailProvider;

    public EmailNotificationSender(ChannelProvider emailProvider) {
        this.emailProvider = emailProvider;
    }

    @Override
    public SendResult send(Notification notification) {
        // Note: In production, you would fetch user data from a repository
        // For demo, we'll create a mock user with email
        User user = new User(notification.getUserId(), "user@example.com", null, null, "en_US");

        if (!canSend(notification)) {
            return SendResult.failure("Missing email address", "NO_EMAIL", "User email not configured", false);
        }

        try {
            Map<String, String> metadata = new HashMap<>();
            metadata.put("templateId", notification.getTemplateId());
            metadata.put("userId", notification.getUserId());

            SendResult result = emailProvider.deliver(
                    user.getEmail(),
                    notification.getSubject(),
                    notification.getBody(),
                    metadata
            );

            if (result.isSuccess()) {
                LOGGER.info("Email sent successfully to: " + user.getEmail());
            }

            return result;
        } catch (Exception e) {
            LOGGER.severe("Error sending email: " + e.getMessage());
            return SendResult.failure("Email send error", "SEND_EXCEPTION", e.getMessage(), true);
        }
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public boolean canSend(Notification notification) {
        return notification != null &&
                notification.getSubject() != null &&
                notification.getBody() != null;
    }
}

