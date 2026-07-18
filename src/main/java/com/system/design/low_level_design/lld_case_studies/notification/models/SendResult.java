package com.system.design.low_level_design.lld_case_studies.notification.models;

import com.system.design.low_level_design.lld_case_studies.notification.enums.NotificationStatus;

/**
 * Represents the result of a send operation.
 */
public class SendResult {
    private final NotificationStatus status;
    private final String message;
    private final String providerMessageId;
    private final String errorCode;
    private final String errorMessage;
    private final boolean retryable;

    private SendResult(NotificationStatus status, String message, String providerMessageId,
                       String errorCode, String errorMessage, boolean retryable) {
        this.status = status;
        this.message = message;
        this.providerMessageId = providerMessageId;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.retryable = retryable;
    }

    public static SendResult success(String message, String providerMessageId) {
        return new SendResult(NotificationStatus.SENT, message, providerMessageId, null, null, false);
    }

    public static SendResult failure(String message, String errorCode, String errorMessage, boolean retryable) {
        return new SendResult(NotificationStatus.FAILED, message, null, errorCode, errorMessage, retryable);
    }

    public static SendResult delivered(String message) {
        return new SendResult(NotificationStatus.DELIVERED, message, null, null, null, false);
    }

    // Getters
    public NotificationStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getProviderMessageId() {
        return providerMessageId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isRetryable() {
        return retryable;
    }

    public boolean isSuccess() {
        return status == NotificationStatus.SENT || status == NotificationStatus.DELIVERED;
    }

    @Override
    public String toString() {
        return "SendResult{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", errorCode='" + errorCode + '\'' +
                '}';
    }
}

