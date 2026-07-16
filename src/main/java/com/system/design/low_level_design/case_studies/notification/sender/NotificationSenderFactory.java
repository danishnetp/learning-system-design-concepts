package com.system.design.low_level_design.case_studies.notification.sender;

import com.system.design.low_level_design.case_studies.notification.provider.FirebasePushProvider;
import com.system.design.low_level_design.case_studies.notification.provider.SmtpEmailProvider;
import com.system.design.low_level_design.case_studies.notification.provider.TwilioSmsProvider;
import com.system.design.low_level_design.case_studies.notification.enums.NotificationChannel;
import com.system.design.low_level_design.case_studies.notification.interfaces.ChannelProvider;
import com.system.design.low_level_design.case_studies.notification.interfaces.NotificationSender;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating and managing notification senders.
 * Uses strategy pattern to select appropriate sender for each channel.
 */
public class NotificationSenderFactory {

    private final Map<NotificationChannel, NotificationSender> senders = new HashMap<>();

    /**
     * Registers a sender for a specific channel.
     *
     * @param channel the notification channel
     * @param sender the sender implementation
     */
    public void registerSender(NotificationChannel channel, NotificationSender sender) {
        senders.put(channel, sender);
    }

    /**
     * Gets the sender for a specific channel.
     *
     * @param channel the notification channel
     * @return the sender for the channel
     * @throws IllegalArgumentException if no sender registered for channel
     */
    public NotificationSender getSender(NotificationChannel channel) {
        NotificationSender sender = senders.get(channel);
        if (sender == null) {
            throw new IllegalArgumentException("No sender registered for channel: " + channel);
        }
        return sender;
    }

    /**
     * Creates a default factory with all standard providers.
     *
     * @return configured factory
     */
    public static NotificationSenderFactory createDefault() {
        NotificationSenderFactory factory = new NotificationSenderFactory();

        // Create providers
        ChannelProvider emailProvider = new SmtpEmailProvider();
        ChannelProvider smsProvider = new TwilioSmsProvider();
        ChannelProvider pushProvider = new FirebasePushProvider();

        // Register senders
        factory.registerSender(NotificationChannel.EMAIL, new EmailNotificationSender(emailProvider));
        factory.registerSender(NotificationChannel.SMS, new SmsNotificationSender(smsProvider));
        factory.registerSender(NotificationChannel.PUSH, new PushNotificationSender(pushProvider));
        factory.registerSender(NotificationChannel.IN_APP, new InAppNotificationSender());

        return factory;
    }

    /**
     * Checks if a sender is registered for a channel.
     *
     * @param channel the notification channel
     * @return true if sender is registered, false otherwise
     */
    public boolean hasSender(NotificationChannel channel) {
        return senders.containsKey(channel);
    }
}

