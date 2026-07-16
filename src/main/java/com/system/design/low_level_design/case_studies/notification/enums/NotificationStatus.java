package com.system.design.low_level_design.case_studies.notification.enums;

/**
 * Enumerates all possible notification statuses throughout its lifecycle.
 */
public enum NotificationStatus {
    CREATED("created", "Notification created"),
    QUEUED("queued", "Notification queued for processing"),
    PROCESSING("processing", "Notification is being processed"),
    SENT("sent", "Notification sent to provider"),
    DELIVERED("delivered", "Notification delivered to recipient"),
    FAILED("failed", "Notification delivery failed"),
    RETRY_SCHEDULED("retry_scheduled", "Retry scheduled for later"),
    CANCELLED("cancelled", "Notification cancelled"),
    SUPPRESSED("suppressed", "Notification suppressed due to user preferences");

    private final String code;
    private final String description;

    NotificationStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public boolean isTerminal() {
        return this == DELIVERED || this == FAILED || this == CANCELLED || this == SUPPRESSED;
    }

    public boolean isRetryable() {
        return this == FAILED || this == RETRY_SCHEDULED;
    }
}

