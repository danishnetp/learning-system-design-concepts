package com.system.design.low_level_design.lld_case_studies.notification.service;

import com.system.design.low_level_design.lld_case_studies.notification.entities.Notification;
import com.system.design.low_level_design.lld_case_studies.notification.interfaces.RetryPolicy;
import java.time.Duration;
import java.util.Random;

/**
 * Retry policy using exponential backoff with jitter.
 */
public class ExponentialBackoffRetryPolicy implements RetryPolicy {

    private final int maxRetries;
    private final long baseDelayMillis;
    private final Random random = new Random();

    public ExponentialBackoffRetryPolicy() {
        this(3, 1000); // 3 retries, 1 second base delay
    }

    public ExponentialBackoffRetryPolicy(int maxRetries, long baseDelayMillis) {
        this.maxRetries = maxRetries;
        this.baseDelayMillis = baseDelayMillis;
    }

    @Override
    public boolean canRetry(Notification notification, int attemptCount) {
        return attemptCount < maxRetries;
    }

    @Override
    public Duration getNextBackoff(int attemptCount) {
        // Exponential: 1s, 2s, 4s, etc. + jitter
        long exponentialDelay = baseDelayMillis * (long) Math.pow(2, attemptCount - 1);
        long jitter = random.nextLong(exponentialDelay / 2); // Add up to 50% jitter
        long totalDelay = exponentialDelay + jitter;
        return Duration.ofMillis(totalDelay);
    }

    @Override
    public int getMaxRetries() {
        return maxRetries;
    }
}

