package com.sunyesle.order_service.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunyesle.order_service.event.OrderPlacedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class OutboxProcessor {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OutboxService outboxService;
    private final ObjectMapper avroObjectMapper;

    public OutboxProcessor(
            KafkaTemplate<String, Object> kafkaTemplate,
            OutboxService outboxService,
            @Qualifier("avroObjectMapper") ObjectMapper avroObjectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.outboxService = outboxService;
        this.avroObjectMapper = avroObjectMapper;
    }

    public void processAll() {
        outboxService.getReadyOutboxes().forEach(this::processInternal);
    }

    private void processInternal(Outbox outbox) {
        try {
            log.info("Outbox {} sending: attempt {}", outbox.getId(), outbox.getFailureCount() + 1);

            Object data = convertToObject(outbox.getEventType(), outbox.getPayload());
            kafkaTemplate.send(outbox.getEventType(), outbox.getAggregateId(), data)
                    .get(5, TimeUnit.SECONDS);

            outboxService.markCompleted(outbox.getId());
            log.info("Outbox {} marked as COMPLETED", outbox.getId());
        } catch (Exception e) {
            log.error("Outbox {} failed: {}", outbox.getId(), e.getMessage());

            if (outbox.getFailureCount() >= 5) {
                outboxService.markFailed(outbox.getId());

                log.error("Outbox {} marked as FAILED", outbox.getId());
            } else {
                long delaySeconds = (long) Math.pow(2, outbox.getFailureCount()) * 5;
                outboxService.scheduleNextRetry(outbox.getId(), Duration.ofSeconds(delaySeconds));

                log.warn("Outbox {} retry scheduled: {}s", outbox.getId(), delaySeconds);
            }
        }
    }

    private Object convertToObject(String eventType, String jsonPayload) throws JsonProcessingException {
        return switch (eventType) {
            case "order-placed" -> avroObjectMapper.readValue(jsonPayload, OrderPlacedEvent.class);
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        };
    }
}
