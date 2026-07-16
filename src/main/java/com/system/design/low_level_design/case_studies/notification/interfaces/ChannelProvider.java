package com.system.design.low_level_design.case_studies.notification.interfaces;

import com.system.design.low_level_design.case_studies.notification.enums.NotificationChannel;
import com.system.design.low_level_design.case_studies.notification.models.SendResult;

import java.util.Map;

/**
 * Interface for external notification provider integration.
 */
public interface ChannelProvider {

    /**
     * Delivers a message through the provider.
     *
     * @param recipient the recipient (email, phone, device token, etc.)
     * @param subject the message subject (for email)
     * @param body the message body
     * @param metadata additional metadata for the provider
     * @return send result with provider response
     */
    SendResult deliver(String recipient, String subject, String body, Map<String, String> metadata);

    /**
     * Returns the notification channel this provider supports.
     *
     * @return notification channel
     */
    NotificationChannel getChannel();

    /**
     * Returns the provider name.
     *
     * @return provider name
     */
    String getProviderName();

    /**
     * Checks if the provider is available for sending.
     *
     * @return true if available, false otherwise
     */
    boolean isAvailable();
}

