package com.system.design.low_level_design.lld_case_studies.notification.service;

import com.system.design.low_level_design.lld_case_studies.notification.entities.NotificationRequest;
import com.system.design.low_level_design.lld_case_studies.notification.enums.NotificationChannel;
import com.system.design.low_level_design.lld_case_studies.notification.exceptions.InvalidNotificationRequestException;
import java.time.Instant;
import java.util.regex.Pattern;

/**
 * Validator for notification requests.
 */
public class NotificationValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@(.+)$"
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[1-9]\\d{1,14}$"
    );

    /**
     * Validates a notification request.
     *
     * @param request the request to validate
     * @throws InvalidNotificationRequestException if request is invalid
     */
    public void validate(NotificationRequest request) {
        if (request == null) {
            throw new InvalidNotificationRequestException("Notification request cannot be null");
        }

        if (request.getUserId() == null || request.getUserId().isEmpty()) {
            throw new InvalidNotificationRequestException("User ID cannot be empty");
        }

        if (request.getType() == null) {
            throw new InvalidNotificationRequestException("Notification type is required");
        }

        if (request.getTemplateId() == null || request.getTemplateId().isEmpty()) {
            throw new InvalidNotificationRequestException("Template ID is required");
        }

        if (request.getChannels() == null || request.getChannels().isEmpty()) {
            throw new InvalidNotificationRequestException("At least one notification channel is required");
        }

        if (request.getScheduledAt() != null && request.getScheduledAt().isBefore(Instant.now())) {
            throw new InvalidNotificationRequestException("Scheduled time cannot be in the past");
        }
    }

    /**
     * Validates email address format.
     *
     * @param email the email to validate
     * @return true if valid email format
     */
    public boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validates phone number format.
     *
     * @param phoneNumber the phone number to validate
     * @return true if valid phone number format
     */
    public boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && PHONE_PATTERN.matcher(phoneNumber).matches();
    }

    /**
     * Validates a device token for push notifications.
     *
     * @param deviceToken the device token to validate
     * @return true if valid device token format
     */
    public boolean isValidDeviceToken(String deviceToken) {
        return deviceToken != null && !deviceToken.isEmpty() && deviceToken.length() >= 10;
    }

    /**
     * Validates if sender has required data for a channel.
     *
     * @param channel the notification channel
     * @param email user's email
     * @param phoneNumber user's phone number
     * @param deviceToken user's device token
     * @return true if sender has required data
     */
    public boolean hasRequiredDataForChannel(NotificationChannel channel, String email, String phoneNumber, String deviceToken) {
        return switch (channel) {
            case EMAIL -> email != null && !email.isEmpty();
            case SMS -> phoneNumber != null && !phoneNumber.isEmpty();
            case PUSH -> deviceToken != null && !deviceToken.isEmpty();
            case IN_APP -> true; // In-app only needs user ID
            case WEBHOOK -> true; // Webhook configured separately
        };
    }
}

