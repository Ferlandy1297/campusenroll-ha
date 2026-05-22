package com.campusenroll.billing.outbox;

import com.campusenroll.billing.billing.Billing;
import com.campusenroll.billing.billing.BillingStatus;
import com.campusenroll.billing.messaging.BillingStatusChangedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BillingOutboxService {

    static final String SERVICE_NAME = "billing-service";
    private static final String AGGREGATE_TYPE = "billing";
    private static final String EVENT_TYPE = "BillingStatusChangedEvent";

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final String billingStatusChangedRoutingKey;

    public BillingOutboxService(
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper,
            @Value("${app.messaging.billing-status-changed-routing-key}") String billingStatusChangedRoutingKey) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
        this.billingStatusChangedRoutingKey = billingStatusChangedRoutingKey;
    }

    public void enqueueBillingStatusChanged(Billing billing, BillingStatus previousStatus, BillingStatus newStatus) {
        BillingStatusChangedEvent event = new BillingStatusChangedEvent(
                UUID.randomUUID(),
                billing.getId(),
                billing.getEnrollmentId(),
                previousStatus.name(),
                newStatus.name(),
                OffsetDateTime.now());

        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setServiceName(SERVICE_NAME);
        outboxEvent.setAggregateType(AGGREGATE_TYPE);
        outboxEvent.setAggregateId(billing.getId());
        outboxEvent.setEventType(EVENT_TYPE);
        outboxEvent.setRoutingKey(billingStatusChangedRoutingKey);
        outboxEvent.setPayload(serialize(event));
        outboxEvent.setStatus(OutboxEventStatus.PENDING);
        outboxEvent.setAttempts(0);
        outboxEvent.setCreatedAt(OffsetDateTime.now());
        outboxEventRepository.save(outboxEvent);
    }

    private String serialize(BillingStatusChangedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize billing outbox payload", ex);
        }
    }
}
