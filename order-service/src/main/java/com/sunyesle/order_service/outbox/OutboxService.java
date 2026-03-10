package com.sunyesle.order_service.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxRepository outboxRepository;

    @Transactional(readOnly = true)
    public List<Outbox> getReadyOutboxes() {
        return outboxRepository.findTop10ByStatusAndNextRetryAtLessThanEqualOrderByNextRetryAtAsc(
                OutboxStatus.NEW,
                LocalDateTime.now()
        );
    }

    @Transactional(readOnly = true)
    public Outbox getOutbox(long outboxId) {
        return findOutboxById(outboxId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCompleted(Long id) {
        Outbox outbox = findOutboxById(id);
        outbox.markCompleted();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long id) {
        Outbox outbox = findOutboxById(id);
        outbox.markFailed();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void scheduleNextRetry(Long id, Duration duration) {
        Outbox outbox = findOutboxById(id);
        outbox.incrementFailureCount();
        outbox.scheduleNextRetry(duration);
    }

    private Outbox findOutboxById(Long id) {
        return outboxRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Outbox not found"));
    }
}
