package com.system.design.low_level_design.lld_case_studies.notification.entities;

import com.system.design.low_level_design.lld_case_studies.notification.enums.NotificationStatus;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a single delivery attempt of a notification.
 */
public class DeliveryAttempt {
    private final String attemptId;
    private final String notificationId;
    private final String providerName;
    private final int attemptNumber;
    private NotificationStatus status;
    private String errorCode;
    private String errorMessage;
    private String providerResponse;
    private final Instant attemptedAt;
    private Instant completedAt;

    public DeliveryAttempt(String notificationId, String providerName, int attemptNumber) {
        this.attemptId = UUID.randomUUID().toString();
        this.notificationId = notificationId;
        this.providerName = providerName;
        this.attemptNumber = attemptNumber;
        this.status = NotificationStatus.PROCESSING;
        this.attemptedAt = Instant.now();
    }

    // Getters and setters
    public String getAttemptId() {
        return attemptId;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public String getProviderName() {
        return providerName;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
        this.completedAt = Instant.now();
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

    public String getProviderResponse() {
        return providerResponse;
    }

    public void setProviderResponse(String providerResponse) {
        this.providerResponse = providerResponse;
    }

    public Instant getAttemptedAt() {
        return attemptedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeliveryAttempt that = (DeliveryAttempt) o;
        return Objects.equals(attemptId, that.attemptId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attemptId);
    }

    @Override
    public String toString() {
        return "DeliveryAttempt{" +
                "attemptId='" + attemptId + '\'' +
                ", notificationId='" + notificationId + '\'' +
                ", providerName='" + providerName + '\'' +
                ", attemptNumber=" + attemptNumber +
                ", status=" + status +
                '}';
    }
}

