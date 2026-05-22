package com.campusenroll.billing.outbox;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    Optional<OutboxEvent> findByIdAndServiceName(Long id, String serviceName);

    @Query("""
            select outboxEvent.id
            from OutboxEvent outboxEvent
            where outboxEvent.serviceName = :serviceName
              and outboxEvent.status = :status
            order by outboxEvent.createdAt asc, outboxEvent.id asc
            """)
    List<Long> findPendingEventIds(
            @Param("serviceName") String serviceName,
            @Param("status") OutboxEventStatus status,
            Pageable pageable);
}
