package com.sunyesle.order_service.outbox.retry;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExponentialRetryPolicy implements OutboxRetryPolicy {
    private final long initialDelay;
    private final long maxDelay;
    private final double multiplier;

    @Override
    public long calculateDelay(int failureCount) {
        long delay = (long) (initialDelay * Math.pow(multiplier, failureCount));
        return Math.min(delay, maxDelay);
    }
}
