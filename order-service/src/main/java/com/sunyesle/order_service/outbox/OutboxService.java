package com.sunyesle.order_service.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxRepository outboxRepository;

    @Transactional(readOnly = true)
    public List<Outbox> getPendingOutboxes() {
        return outboxRepository.findTop10ByProcessedAtIsNullOrderByCreatedAtAsc();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(Long id) {
        Outbox outbox = outboxRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Outbox not found"));

        outbox.processed();
    }
}
