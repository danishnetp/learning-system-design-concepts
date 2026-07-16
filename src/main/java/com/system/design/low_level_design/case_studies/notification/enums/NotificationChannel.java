package com.system.design.low_level_design.case_studies.notification.enums;

/**
 * Enumerates all supported notification channels.
 */
public enum NotificationChannel {
    EMAIL("email"),
    SMS("sms"),
    PUSH("push"),
    IN_APP("in_app"),
    WEBHOOK("webhook");

    private final String displayName;

    NotificationChannel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

