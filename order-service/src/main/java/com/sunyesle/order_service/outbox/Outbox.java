package com.sunyesle.order_service.outbox;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "outbox")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Outbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String aggregateId;
    private String eventType;
    private String payload;
    @Enumerated(EnumType.STRING)
    private OutboxStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private int failureCount;
    private LocalDateTime nextRetryAt;

    public Outbox(String aggregateId, String eventType, String payload) {
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = OutboxStatus.NEW;
        this.createdAt = LocalDateTime.now();
        this.failureCount = 0;
        this.nextRetryAt = LocalDateTime.now();
    }

    public void markCompleted() {
        this.completedAt = LocalDateTime.now();
        this.status = OutboxStatus.COMPLETED;
    }

    public void markFailed() {
        this.status = OutboxStatus.FAILED;
    }

    public void incrementFailureCount() {
        this.failureCount++;
    }

    public void scheduleNextRetry(Duration delay) {
        this.nextRetryAt = LocalDateTime.now().plus(delay);
    }
}
