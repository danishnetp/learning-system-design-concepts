package com.system.design.low_level_design.case_studies.notification.provider;

import com.system.design.low_level_design.case_studies.notification.enums.NotificationChannel;
import com.system.design.low_level_design.case_studies.notification.interfaces.ChannelProvider;
import com.system.design.low_level_design.case_studies.notification.models.SendResult;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Mock Email provider for demonstration (simulates SMTP).
 */
public class SmtpEmailProvider implements ChannelProvider {

    private static final Logger LOGGER = Logger.getLogger(SmtpEmailProvider.class.getName());
    private static final String PROVIDER_NAME = "SMTP";

    @Override
    public SendResult deliver(String recipient, String subject, String body, Map<String, String> metadata) {
        try {
            // Simulate email delivery
            String messageId = "msg-" + UUID.randomUUID();
            LOGGER.info("Delivering email to: " + recipient + " with subject: " + subject);

            // Simulate occasional failures for demonstration
            if (Math.random() < 0.05) { // 5% failure rate
                return SendResult.failure("Email delivery failed", "SMTP_ERROR", "Connection timeout", true);
            }

            return SendResult.success("Email sent successfully", messageId);
        } catch (Exception e) {
            LOGGER.severe("Error delivering email: " + e.getMessage());
            return SendResult.failure("Email delivery error", "SMTP_EXCEPTION", e.getMessage(), true);
        }
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public boolean isAvailable() {
        // In production, check actual provider health/availability
        return true;
    }
}

