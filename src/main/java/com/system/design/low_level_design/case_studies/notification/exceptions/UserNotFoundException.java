package com.system.design.low_level_design.case_studies.notification.exceptions;

/**
 * Thrown when a user is not found.
 */
public class UserNotFoundException extends NotificationException {
    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

