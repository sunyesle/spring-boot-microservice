package com.sunyesle.order_service.outbox;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxRepository extends JpaRepository<Outbox, Long> {
    List<Outbox> findTop10ByStatusAndNextRetryAtLessThanEqualOrderByNextRetryAtAsc(
            OutboxStatus status,
            LocalDateTime now
    );

    @Query("""
                SELECT o
                FROM Outbox o
                WHERE o.status = :status
                AND o.nextRetryAt <= :now
                AND o.createdAt <= :createdBefore
                ORDER BY o.nextRetryAt ASC
            """)
    List<Outbox> findReadyOutboxes(
            @Param("status") OutboxStatus outboxStatus,
            @Param("now") LocalDateTime now,
            @Param("createdBefore") LocalDateTime createdBefore,
            Pageable pageable
    );
}
