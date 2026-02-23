package com.sunyesle.order_service.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OutboxEventListener {

    private final OutboxWriter outboxWriter;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void saveOutbox(OutboxEvent event) {
        outboxWriter.write(event.aggregateId(), event.eventType(), event.payload());
    }
}
