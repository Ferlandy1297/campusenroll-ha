package com.campusenroll.billing.outbox;

import static org.assertj.core.api.Assertions.assertThat;

import com.campusenroll.billing.billing.Billing;
import com.campusenroll.billing.billing.BillingStatus;
import com.campusenroll.billing.messaging.BillingStatusChangedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BillingOutboxServiceTest {

    @Test
    void shouldPersistPendingBillingStatusChangedOutboxRow() throws Exception {
        RepositoryState state = new RepositoryState();
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        BillingOutboxService outboxService =
                new BillingOutboxService(repository(state), objectMapper, "billing.status.changed");

        Billing billing = new Billing();
        billing.setId(20L);
        billing.setEnrollmentId(100L);
        billing.setAmount(new BigDecimal("150.75"));
        billing.setCurrency("USD");
        billing.setStatus(BillingStatus.PAID);
        billing.setCreatedAt(OffsetDateTime.parse("2026-05-21T10:15:30Z"));

        outboxService.enqueueBillingStatusChanged(billing, BillingStatus.PENDING, BillingStatus.PAID);

        assertThat(state.storage).hasSize(1);
        OutboxEvent storedEvent = state.storage.get(1L);
        assertThat(storedEvent.getServiceName()).isEqualTo("billing-service");
        assertThat(storedEvent.getAggregateType()).isEqualTo("billing");
        assertThat(storedEvent.getAggregateId()).isEqualTo(20L);
        assertThat(storedEvent.getEventType()).isEqualTo("BillingStatusChangedEvent");
        assertThat(storedEvent.getRoutingKey()).isEqualTo("billing.status.changed");
        assertThat(storedEvent.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(storedEvent.getAttempts()).isZero();
        assertThat(storedEvent.getCreatedAt()).isNotNull();
        assertThat(storedEvent.getPublishedAt()).isNull();

        BillingStatusChangedEvent payload =
                objectMapper.readValue(storedEvent.getPayload(), BillingStatusChangedEvent.class);
        assertThat(payload.billingId()).isEqualTo(20L);
        assertThat(payload.enrollmentId()).isEqualTo(100L);
        assertThat(payload.previousStatus()).isEqualTo("PENDING");
        assertThat(payload.newStatus()).isEqualTo("PAID");
        assertThat(payload.eventId()).isNotNull();
        assertThat(payload.occurredAt()).isNotNull();
    }

    private static OutboxEventRepository repository(RepositoryState state) {
        InvocationHandler handler = new OutboxEventRepositoryHandler(state);
        return (OutboxEventRepository) Proxy.newProxyInstance(
                OutboxEventRepository.class.getClassLoader(),
                new Class<?>[] {OutboxEventRepository.class},
                handler);
    }

    private static final class RepositoryState {
        private final Map<Long, OutboxEvent> storage = new HashMap<>();
        private long sequence = 1L;
    }

    private static final class OutboxEventRepositoryHandler implements InvocationHandler {

        private final RepositoryState state;

        private OutboxEventRepositoryHandler(RepositoryState state) {
            this.state = state;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "save", "saveAndFlush" -> save((OutboxEvent) args[0]);
                case "toString" -> "BillingOutboxEventRepositoryTestProxy";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                default -> throw new UnsupportedOperationException("Unsupported repository method: " + method.getName());
            };
        }

        private OutboxEvent save(OutboxEvent outboxEvent) {
            if (outboxEvent.getId() == null) {
                outboxEvent.setId(state.sequence++);
            }
            state.storage.put(outboxEvent.getId(), outboxEvent);
            return outboxEvent;
        }
    }
}
