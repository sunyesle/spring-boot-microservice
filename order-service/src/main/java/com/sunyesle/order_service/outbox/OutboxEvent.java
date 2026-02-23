package com.sunyesle.order_service.outbox;

public record OutboxEvent(String aggregateId, String eventType, Object payload) {
}
