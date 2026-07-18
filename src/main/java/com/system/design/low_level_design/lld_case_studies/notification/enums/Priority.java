package com.system.design.low_level_design.lld_case_studies.notification.enums;

/**
 * Enumerates priority levels for notifications.
 */
public enum Priority {
    LOW(0, "low"),
    MEDIUM(1, "medium"),
    HIGH(2, "high"),
    CRITICAL(3, "critical");

    private final int level;
    private final String displayName;

    Priority(int level, String displayName) {
        this.level = level;
        this.displayName = displayName;
    }

    public int getLevel() {
        return level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isHigherThan(Priority other) {
        return this.level > other.level;
    }
}

