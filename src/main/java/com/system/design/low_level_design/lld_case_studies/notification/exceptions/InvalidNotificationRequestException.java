package com.system.design.low_level_design.lld_case_studies.notification.exceptions;

/**
 * Thrown when a notification request is invalid.
 */
public class InvalidNotificationRequestException extends NotificationException {
    public InvalidNotificationRequestException(String message) {
        super(message);
    }

    public InvalidNotificationRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}

