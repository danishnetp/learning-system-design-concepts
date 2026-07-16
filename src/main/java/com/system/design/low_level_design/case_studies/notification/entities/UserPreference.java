package com.system.design.low_level_design.case_studies.notification.entities;

import com.system.design.low_level_design.case_studies.notification.enums.NotificationChannel;
import com.system.design.low_level_design.case_studies.notification.enums.NotificationType;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Represents user preferences for receiving notifications.
 */
public class UserPreference {
    private final String userId;
    private final NotificationType type;
    private final NotificationChannel channel;
    private boolean enabled;
    private LocalTime quietHoursStart;
    private LocalTime quietHoursEnd;
    private final Instant createdAt;
    private Instant updatedAt;

    public UserPreference(String userId, NotificationType type, NotificationChannel channel) {
        this.userId = userId;
        this.type = type;
        this.channel = channel;
        this.enabled = true; // Default to enabled
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public NotificationType getType() {
        return type;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.updatedAt = Instant.now();
    }

    public LocalTime getQuietHoursStart() {
        return quietHoursStart;
    }

    public void setQuietHoursStart(LocalTime quietHoursStart) {
        this.quietHoursStart = quietHoursStart;
        this.updatedAt = Instant.now();
    }

    public LocalTime getQuietHoursEnd() {
        return quietHoursEnd;
    }

    public void setQuietHoursEnd(LocalTime quietHoursEnd) {
        this.quietHoursEnd = quietHoursEnd;
        this.updatedAt = Instant.now();
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public boolean isInQuietHours() {
        if (quietHoursStart == null || quietHoursEnd == null) {
            return false;
        }
        LocalTime now = LocalTime.now();
        if (quietHoursStart.isBefore(quietHoursEnd)) {
            return now.isAfter(quietHoursStart) && now.isBefore(quietHoursEnd);
        } else {
            // Quiet hours span midnight
            return now.isAfter(quietHoursStart) || now.isBefore(quietHoursEnd);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPreference that = (UserPreference) o;
        return Objects.equals(userId, that.userId) &&
                type == that.type &&
                channel == that.channel;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, type, channel);
    }

    @Override
    public String toString() {
        return "UserPreference{" +
                "userId='" + userId + '\'' +
                ", type=" + type +
                ", channel=" + channel +
                ", enabled=" + enabled +
                '}';
    }
}

