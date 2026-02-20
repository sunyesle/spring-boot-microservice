package com.sunyesle.order_service.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxScheduler {

    private final OutboxProcessor outboxProcessor;

    @Scheduled(fixedDelay = 5000)
    public void processOutbox() {
        outboxProcessor.processAll();
    }
}
