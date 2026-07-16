package com.system.design.low_level_design.case_studies.notification.exceptions;

/**
 * Thrown when a provider delivery fails.
 */
public class ProviderException extends NotificationException {
    private final String providerName;
    private final String errorCode;
    private final boolean retryable;

    public ProviderException(String message, String providerName, String errorCode, boolean retryable) {
        super(message);
        this.providerName = providerName;
        this.errorCode = errorCode;
        this.retryable = retryable;
    }

    public ProviderException(String message, String providerName, String errorCode, boolean retryable, Throwable cause) {
        super(message, cause);
        this.providerName = providerName;
        this.errorCode = errorCode;
        this.retryable = retryable;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public boolean isRetryable() {
        return retryable;
    }
}

