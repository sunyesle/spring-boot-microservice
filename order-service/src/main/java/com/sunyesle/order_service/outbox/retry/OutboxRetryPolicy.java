package com.sunyesle.order_service.outbox.retry;

public interface OutboxRetryPolicy {
    long calculateDelay(int failureCount);
}
