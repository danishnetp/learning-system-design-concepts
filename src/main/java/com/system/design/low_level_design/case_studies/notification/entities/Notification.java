package com.system.design.low_level_design.case_studies.notification.entities;

import com.system.design.low_level_design.case_studies.notification.enums.NotificationChannel;
import com.system.design.low_level_design.case_studies.notification.enums.NotificationStatus;
import com.system.design.low_level_design.case_studies.notification.enums.NotificationType;
import com.system.design.low_level_design.case_studies.notification.enums.Priority;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an internal notification object with lifecycle tracking.
 */
public class Notification {
    private final String notificationId;
    private final String requestId;
    private final String userId;
    private final NotificationChannel channel;
    private final NotificationType type;
    private final String templateId;
    private final String subject;
    private final String body;
    private NotificationStatus status;
    private Priority priority;
    private String providerName;
    private String providerMessageId;
    private String errorCode;
    private String errorMessage;
    private int attemptCount;
    private Instant scheduledAt;
    private Instant sentAt;
    private Instant deliveredAt;
    private final Instant createdAt;
    private Instant updatedAt;

    public Notification(String requestId, String userId, NotificationChannel channel,
                        NotificationType type, String templateId, String subject, String body) {
        this.notificationId = UUID.randomUUID().toString();
        this.requestId = requestId;
        this.userId = userId;
        this.channel = channel;
        this.type = type;
        this.templateId = templateId;
        this.subject = subject;
        this.body = body;
        this.status = NotificationStatus.CREATED;
        this.priority = Priority.MEDIUM;
        this.attemptCount = 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // Getters and setters
    public String getNotificationId() {
        return notificationId;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getUserId() {
        return userId;
    }

    public NotificationChannel getChannel() {
        return channel;
    }

    public NotificationType getType() {
        return type;
    }

    public String getTemplateId() {
        return templateId;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getProviderMessageId() {
        return providerMessageId;
    }

    public void setProviderMessageId(String providerMessageId) {
        this.providerMessageId = providerMessageId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void incrementAttemptCount() {
        this.attemptCount++;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Instant scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(Instant deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return Objects.equals(notificationId, that.notificationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(notificationId);
    }

    @Override
    public String toString() {
        return "Notification{" +
                "notificationId='" + notificationId + '\'' +
                ", userId='" + userId + '\'' +
                ", channel=" + channel +
                ", status=" + status +
                '}';
    }
}

