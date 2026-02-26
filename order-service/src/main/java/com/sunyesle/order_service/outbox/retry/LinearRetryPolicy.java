package com.sunyesle.order_service.outbox.retry;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LinearRetryPolicy implements OutboxRetryPolicy {
    private final long initialDelay;
    private final long maxDelay;
    private final long increment;

    @Override
    public long calculateDelay(int failureCount) {
        long delay = initialDelay + failureCount * increment;
        return Math.min(delay, maxDelay);
    }
}
