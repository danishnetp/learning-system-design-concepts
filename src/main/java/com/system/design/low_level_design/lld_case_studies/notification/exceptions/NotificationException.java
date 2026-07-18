package com.system.design.low_level_design.lld_case_studies.notification.exceptions;

/**
 * Base exception for notification service.
 */
public class NotificationException extends RuntimeException {
    public NotificationException(String message) {
        super(message);
    }

    public NotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}

