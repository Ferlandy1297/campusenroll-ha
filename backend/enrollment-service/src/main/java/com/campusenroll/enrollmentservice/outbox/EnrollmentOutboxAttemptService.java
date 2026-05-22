package com.campusenroll.enrollmentservice.outbox;

import com.campusenroll.enrollmentservice.messaging.EnrollmentCreatedEvent;
import com.campusenroll.enrollmentservice.messaging.EnrollmentEventPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnrollmentOutboxAttemptService {

    private final OutboxEventRepository outboxEventRepository;
    private final EnrollmentEventPublisher enrollmentEventPublisher;
    private final ObjectMapper objectMapper;
    private final int maxAttempts;

    public EnrollmentOutboxAttemptService(
            OutboxEventRepository outboxEventRepository,
            EnrollmentEventPublisher enrollmentEventPublisher,
            ObjectMapper objectMapper,
            @Value("${app.outbox.publisher.max-attempts:5}") int maxAttempts) {
        this.outboxEventRepository = outboxEventRepository;
        this.enrollmentEventPublisher = enrollmentEventPublisher;
        this.objectMapper = objectMapper;
        this.maxAttempts = maxAttempts;
    }

    @Transactional
    public void publishPendingEvent(Long outboxEventId) {
        OutboxEvent outboxEvent = outboxEventRepository
                .findByIdAndServiceName(outboxEventId, EnrollmentOutboxService.SERVICE_NAME)
                .orElse(null);

        if (outboxEvent == null || outboxEvent.getStatus() != OutboxEventStatus.PENDING) {
            return;
        }

        EnrollmentCreatedEvent event = deserialize(outboxEvent.getPayload());
        enrollmentEventPublisher.publishEnrollmentCreated(event);
        outboxEvent.setStatus(OutboxEventStatus.PUBLISHED);
        outboxEvent.setPublishedAt(OffsetDateTime.now());
        outboxEvent.setLastError(null);
        outboxEventRepository.saveAndFlush(outboxEvent);
    }

    @Transactional
    public void markFailure(Long outboxEventId, String errorMessage) {
        OutboxEvent outboxEvent = outboxEventRepository
                .findByIdAndServiceName(outboxEventId, EnrollmentOutboxService.SERVICE_NAME)
                .orElse(null);

        if (outboxEvent == null || outboxEvent.getStatus() != OutboxEventStatus.PENDING) {
            return;
        }

        int nextAttempts = outboxEvent.getAttempts() + 1;
        outboxEvent.setAttempts(nextAttempts);
        outboxEvent.setLastError(normalizeError(errorMessage));
        if (nextAttempts >= maxAttempts) {
            outboxEvent.setStatus(OutboxEventStatus.FAILED);
        }
        outboxEventRepository.saveAndFlush(outboxEvent);
    }

    private EnrollmentCreatedEvent deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, EnrollmentCreatedEvent.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to deserialize enrollment outbox payload", ex);
        }
    }

    private String normalizeError(String errorMessage) {
        if (errorMessage == null || errorMessage.isBlank()) {
            return "Unexpected outbox publishing error";
        }
        return errorMessage;
    }
}
