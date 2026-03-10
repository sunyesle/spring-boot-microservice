package com.sunyesle.order_service.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OutboxWriter {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper avroObjectMapper;

    public OutboxWriter(
            OutboxRepository outboxRepository,
            @Qualifier("avroObjectMapper") ObjectMapper avroObjectMapper) {
        this.outboxRepository = outboxRepository;
        this.avroObjectMapper = avroObjectMapper;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Outbox write(String aggregateId, String eventType, Object event) {
        try {
            String payload = avroObjectMapper.writeValueAsString(event);
            Outbox outbox = new Outbox(
                    aggregateId,
                    eventType,
                    payload
            );
            return outboxRepository.save(outbox);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Serialization failed", e);
        }
    }
}
