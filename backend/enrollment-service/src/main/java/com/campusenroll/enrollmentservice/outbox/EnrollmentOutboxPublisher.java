package com.campusenroll.enrollmentservice.outbox;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentOutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentOutboxPublisher.class);

    private final OutboxEventRepository outboxEventRepository;
    private final EnrollmentOutboxAttemptService enrollmentOutboxAttemptService;
    private final int batchSize;

    public EnrollmentOutboxPublisher(
            OutboxEventRepository outboxEventRepository,
            EnrollmentOutboxAttemptService enrollmentOutboxAttemptService,
            @Value("${app.outbox.publisher.batch-size:20}") int batchSize) {
        this.outboxEventRepository = outboxEventRepository;
        this.enrollmentOutboxAttemptService = enrollmentOutboxAttemptService;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${app.outbox.publisher.fixed-delay-ms:5000}")
    public void publishPendingEvents() {
        List<Long> pendingEventIds = outboxEventRepository.findPendingEventIds(
                EnrollmentOutboxService.SERVICE_NAME,
                OutboxEventStatus.PENDING,
                PageRequest.of(0, batchSize));

        pendingEventIds.forEach(this::publishPendingEventSafely);
    }

    private void publishPendingEventSafely(Long outboxEventId) {
        try {
            enrollmentOutboxAttemptService.publishPendingEvent(outboxEventId);
        } catch (RuntimeException ex) {
            enrollmentOutboxAttemptService.markFailure(outboxEventId, ex.getMessage());
            log.warn(
                    "Failed to publish enrollment outbox event id={} reason={}",
                    outboxEventId,
                    ex.getMessage());
        }
    }
}
