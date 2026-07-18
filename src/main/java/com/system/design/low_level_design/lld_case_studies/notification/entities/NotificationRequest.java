package com.system.design.low_level_design.lld_case_studies.notification.entities;

import com.system.design.low_level_design.lld_case_studies.notification.enums.NotificationChannel;
import com.system.design.low_level_design.lld_case_studies.notification.enums.NotificationType;
import com.system.design.low_level_design.lld_case_studies.notification.enums.Priority;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Represents an incoming notification send request from a caller.
 */
public class NotificationRequest {
    private final String requestId;
    private final String userId;
    private final NotificationType type;
    private final String templateId;
    private final Set<NotificationChannel> channels;
    private final Map<String, Object> payload;
    private final Priority priority;
    private final Instant scheduledAt;
    private final String idempotencyKey;
    private final Instant createdAt;

    public NotificationRequest(String userId, NotificationType type, String templateId,
                               Set<NotificationChannel> channels, Map<String, Object> payload) {
        this(userId, type, templateId, channels, payload, Priority.MEDIUM, null, null);
    }

    public NotificationRequest(String userId, NotificationType type, String templateId,
                               Set<NotificationChannel> channels, Map<String, Object> payload,
                               Priority priority, Instant scheduledAt, String idempotencyKey) {
        this.requestId = UUID.randomUUID().toString();
        this.userId = userId;
        this.type = type;
        this.templateId = templateId;
        this.channels = channels;
        this.payload = payload;
        this.priority = priority;
        this.scheduledAt = scheduledAt;
        this.idempotencyKey = idempotencyKey != null ? idempotencyKey : requestId;
        this.createdAt = Instant.now();
    }

    // Getters
    public String getRequestId() {
        return requestId;
    }

    public String getUserId() {
        return userId;
    }

    public NotificationType getType() {
        return type;
    }

    public String getTemplateId() {
        return templateId;
    }

    public Set<NotificationChannel> getChannels() {
        return channels;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public Priority getPriority() {
        return priority;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isScheduled() {
        return scheduledAt != null && scheduledAt.isAfter(Instant.now());
    }

    @Override
    public String toString() {
        return "NotificationRequest{" +
                "requestId='" + requestId + '\'' +
                ", userId='" + userId + '\'' +
                ", type=" + type +
                ", channels=" + channels +
                '}';
    }
}

