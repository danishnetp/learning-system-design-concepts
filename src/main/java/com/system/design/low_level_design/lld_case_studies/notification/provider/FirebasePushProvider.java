package com.system.design.low_level_design.lld_case_studies.notification.provider;

import com.system.design.low_level_design.lld_case_studies.notification.enums.NotificationChannel;
import com.system.design.low_level_design.lld_case_studies.notification.interfaces.ChannelProvider;
import com.system.design.low_level_design.lld_case_studies.notification.models.SendResult;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Mock Firebase Cloud Messaging provider for demonstration.
 */
public class FirebasePushProvider implements ChannelProvider {

    private static final Logger LOGGER = Logger.getLogger(FirebasePushProvider.class.getName());
    private static final String PROVIDER_NAME = "Firebase";

    @Override
    public SendResult deliver(String recipient, String subject, String body, Map<String, String> metadata) {
        try {
            // Validate device token
            if (recipient == null || recipient.length() < 10) {
                return SendResult.failure("Invalid device token", "INVALID_DEVICE_TOKEN", "Device token is invalid", false);
            }

            String messageId = "fcm-" + UUID.randomUUID();
            LOGGER.info("Delivering push notification to device: " + recipient);

            // Simulate occasional failures
            if (Math.random() < 0.02) { // 2% failure rate
                return SendResult.failure("Push delivery failed", "DEVICE_UNREGISTERED", "Device token expired", false);
            }

            return SendResult.success("Push sent successfully", messageId);
        } catch (Exception e) {
            LOGGER.severe("Error delivering push: " + e.getMessage());
            return SendResult.failure("Push delivery error", "FCM_EXCEPTION", e.getMessage(), true);
        }
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.PUSH;
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}

