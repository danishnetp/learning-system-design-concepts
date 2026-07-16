package com.system.design.low_level_design.case_studies.notification.provider;

import com.system.design.low_level_design.case_studies.notification.enums.NotificationChannel;
import com.system.design.low_level_design.case_studies.notification.interfaces.ChannelProvider;
import com.system.design.low_level_design.case_studies.notification.models.SendResult;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Mock SMS provider for demonstration (simulates Twilio).
 */
public class TwilioSmsProvider implements ChannelProvider {

    private static final Logger LOGGER = Logger.getLogger(TwilioSmsProvider.class.getName());
    private static final String PROVIDER_NAME = "Twilio";

    @Override
    public SendResult deliver(String recipient, String subject, String body, Map<String, String> metadata) {
        try {
            // Validate phone number
            if (recipient == null || !recipient.matches("^\\+?[1-9]\\d{1,14}$")) {
                return SendResult.failure("Invalid phone number", "INVALID_PHONE", "Phone number format is invalid", false);
            }

            String messageId = "sms-" + UUID.randomUUID();
            LOGGER.info("Delivering SMS to: " + recipient);

            // Simulate occasional failures
            if (Math.random() < 0.03) { // 3% failure rate
                return SendResult.failure("SMS delivery failed", "SMS_QUOTA_EXCEEDED", "Provider quota exceeded", true);
            }

            return SendResult.success("SMS sent successfully", messageId);
        } catch (Exception e) {
            LOGGER.severe("Error delivering SMS: " + e.getMessage());
            return SendResult.failure("SMS delivery error", "SMS_EXCEPTION", e.getMessage(), true);
        }
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
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

