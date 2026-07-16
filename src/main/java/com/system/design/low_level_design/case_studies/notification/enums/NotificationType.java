package com.system.design.low_level_design.case_studies.notification.enums;

/**
 * Enumerates all notification types.
 */
public enum NotificationType {
    TRANSACTIONAL("transactional"),
    PROMOTIONAL("promotional"),
    SECURITY("security"),
    SYSTEM_ALERT("system_alert");

    private final String displayName;

    NotificationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

