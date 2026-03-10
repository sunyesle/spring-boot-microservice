package com.sunyesle.order_service.outbox;

import lombok.Getter;

@Getter
public final class OutboxEvent {
    private final String aggregateId;
    private final String eventType;
    private final Object data;
    private Long outboxId;

    public OutboxEvent(String aggregateId, String eventType, Object data) {
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.data = data;
    }

    void setOutboxId(Long outboxId) {
        this.outboxId = outboxId;
    }
}
