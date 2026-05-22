package com.campusenroll.billing.outbox;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BillingOutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(BillingOutboxPublisher.class);

    private final OutboxEventRepository outboxEventRepository;
    private final BillingOutboxAttemptService billingOutboxAttemptService;
    private final int batchSize;

    public BillingOutboxPublisher(
            OutboxEventRepository outboxEventRepository,
            BillingOutboxAttemptService billingOutboxAttemptService,
            @Value("${app.outbox.publisher.batch-size:20}") int batchSize) {
        this.outboxEventRepository = outboxEventRepository;
        this.billingOutboxAttemptService = billingOutboxAttemptService;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${app.outbox.publisher.fixed-delay-ms:5000}")
    public void publishPendingEvents() {
        List<Long> pendingEventIds = outboxEventRepository.findPendingEventIds(
                BillingOutboxService.SERVICE_NAME,
                OutboxEventStatus.PENDING,
                PageRequest.of(0, batchSize));

        pendingEventIds.forEach(this::publishPendingEventSafely);
    }

    private void publishPendingEventSafely(Long outboxEventId) {
        try {
            billingOutboxAttemptService.publishPendingEvent(outboxEventId);
        } catch (RuntimeException ex) {
            billingOutboxAttemptService.markFailure(outboxEventId, ex.getMessage());
            log.warn(
                    "Failed to publish billing outbox event id={} reason={}",
                    outboxEventId,
                    ex.getMessage());
        }
    }
}
