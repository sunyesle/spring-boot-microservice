package com.sunyesle.order_service.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OutboxEventListener {

    private final OutboxWriter outboxWriter;
    private final OutboxProcessor outboxProcessor;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void saveOutbox(OutboxEvent event) {
        Outbox outbox = outboxWriter.write(event.getAggregateId(), event.getEventType(), event.getData());
        event.setOutboxId(outbox.getId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void processOutbox(OutboxEvent event) {
        outboxProcessor.process(event.getOutboxId());
    }
}
