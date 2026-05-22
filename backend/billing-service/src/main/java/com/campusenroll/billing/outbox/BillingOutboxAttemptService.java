package com.campusenroll.billing.outbox;

import com.campusenroll.billing.messaging.BillingEventPublisher;
import com.campusenroll.billing.messaging.BillingStatusChangedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BillingOutboxAttemptService {

    private final OutboxEventRepository outboxEventRepository;
    private final BillingEventPublisher billingEventPublisher;
    private final ObjectMapper objectMapper;
    private final int maxAttempts;

    public BillingOutboxAttemptService(
            OutboxEventRepository outboxEventRepository,
            BillingEventPublisher billingEventPublisher,
            ObjectMapper objectMapper,
            @Value("${app.outbox.publisher.max-attempts:5}") int maxAttempts) {
        this.outboxEventRepository = outboxEventRepository;
        this.billingEventPublisher = billingEventPublisher;
        this.objectMapper = objectMapper;
        this.maxAttempts = maxAttempts;
    }

    @Transactional
    public void publishPendingEvent(Long outboxEventId) {
        OutboxEvent outboxEvent = outboxEventRepository
                .findByIdAndServiceName(outboxEventId, BillingOutboxService.SERVICE_NAME)
                .orElse(null);

        if (outboxEvent == null || outboxEvent.getStatus() != OutboxEventStatus.PENDING) {
            return;
        }

        BillingStatusChangedEvent event = deserialize(outboxEvent.getPayload());
        billingEventPublisher.publishBillingStatusChanged(event);
        outboxEvent.setStatus(OutboxEventStatus.PUBLISHED);
        outboxEvent.setPublishedAt(OffsetDateTime.now());
        outboxEvent.setLastError(null);
        outboxEventRepository.saveAndFlush(outboxEvent);
    }

    @Transactional
    public void markFailure(Long outboxEventId, String errorMessage) {
        OutboxEvent outboxEvent = outboxEventRepository
                .findByIdAndServiceName(outboxEventId, BillingOutboxService.SERVICE_NAME)
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

    private BillingStatusChangedEvent deserialize(String payload) {
        try {
            return objectMapper.readValue(payload, BillingStatusChangedEvent.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to deserialize billing outbox payload", ex);
        }
    }

    private String normalizeError(String errorMessage) {
        if (errorMessage == null || errorMessage.isBlank()) {
            return "Unexpected outbox publishing error";
        }
        return errorMessage;
    }
}
