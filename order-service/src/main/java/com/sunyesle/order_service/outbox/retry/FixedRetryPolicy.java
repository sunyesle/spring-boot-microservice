package com.sunyesle.order_service.outbox.retry;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FixedRetryPolicy implements OutboxRetryPolicy {
    private final long delay;

    @Override
    public long calculateDelay(int failureCount) {
        return delay;
    }
}
