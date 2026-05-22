package com.campusenroll.billing.outbox;

import static org.assertj.core.api.Assertions.assertThat;

import com.campusenroll.billing.messaging.BillingEventPublisher;
import com.campusenroll.billing.messaging.BillingStatusChangedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class BillingOutboxAttemptServiceTest {

    @Test
    void shouldPublishPendingBillingOutboxEventAndMarkItPublished() throws Exception {
        RepositoryState state = new RepositoryState();
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        BillingStatusChangedEvent event = new BillingStatusChangedEvent(
                UUID.randomUUID(),
                20L,
                100L,
                "PENDING",
                "PAID",
                OffsetDateTime.parse("2026-05-21T10:15:30Z"));
        OutboxEvent outboxEvent = outboxEvent(objectMapper.writeValueAsString(event));
        persist(state, outboxEvent);

        RecordingBillingEventPublisher publisher = new RecordingBillingEventPublisher();
        BillingOutboxAttemptService attemptService =
                new BillingOutboxAttemptService(repository(state), publisher, objectMapper, 5);

        attemptService.publishPendingEvent(outboxEvent.getId());

        assertThat(publisher.publishedEvents).hasSize(1);
        assertThat(publisher.publishedEvents.get(0).billingId()).isEqualTo(20L);
        assertThat(outboxEvent.getStatus()).isEqualTo(OutboxEventStatus.PUBLISHED);
        assertThat(outboxEvent.getPublishedAt()).isNotNull();
        assertThat(outboxEvent.getLastError()).isNull();
    }

    @Test
    void shouldMarkBillingOutboxEventAsFailedAfterMaxAttempts() {
        RepositoryState state = new RepositoryState();
        OutboxEvent outboxEvent = outboxEvent("{\"ignored\":true}");
        outboxEvent.setAttempts(4);
        persist(state, outboxEvent);

        BillingOutboxAttemptService attemptService = new BillingOutboxAttemptService(
                repository(state),
                event -> {},
                new ObjectMapper().registerModule(new JavaTimeModule()),
                5);

        attemptService.markFailure(outboxEvent.getId(), "rabbitmq offline");

        assertThat(outboxEvent.getAttempts()).isEqualTo(5);
        assertThat(outboxEvent.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
        assertThat(outboxEvent.getLastError()).isEqualTo("rabbitmq offline");
    }

    private static OutboxEvent outboxEvent(String payload) {
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setServiceName("billing-service");
        outboxEvent.setAggregateType("billing");
        outboxEvent.setAggregateId(20L);
        outboxEvent.setEventType("BillingStatusChangedEvent");
        outboxEvent.setRoutingKey("billing.status.changed");
        outboxEvent.setPayload(payload);
        outboxEvent.setStatus(OutboxEventStatus.PENDING);
        outboxEvent.setAttempts(0);
        outboxEvent.setCreatedAt(OffsetDateTime.parse("2026-05-21T10:15:30Z"));
        return outboxEvent;
    }

    private static OutboxEventRepository repository(RepositoryState state) {
        InvocationHandler handler = new OutboxEventRepositoryHandler(state);
        return (OutboxEventRepository) Proxy.newProxyInstance(
                OutboxEventRepository.class.getClassLoader(),
                new Class<?>[] {OutboxEventRepository.class},
                handler);
    }

    private static OutboxEvent persist(RepositoryState state, OutboxEvent outboxEvent) {
        if (outboxEvent.getId() == null) {
            outboxEvent.setId(state.sequence++);
        }
        state.storage.put(outboxEvent.getId(), outboxEvent);
        return outboxEvent;
    }

    private static final class RepositoryState {
        private final Map<Long, OutboxEvent> storage = new HashMap<>();
        private long sequence = 1L;
    }

    private static final class RecordingBillingEventPublisher implements BillingEventPublisher {
        private final List<BillingStatusChangedEvent> publishedEvents = new ArrayList<>();

        @Override
        public void publishBillingStatusChanged(BillingStatusChangedEvent event) {
            publishedEvents.add(event);
        }
    }

    private static final class OutboxEventRepositoryHandler implements InvocationHandler {

        private final RepositoryState state;

        private OutboxEventRepositoryHandler(RepositoryState state) {
            this.state = state;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "findByIdAndServiceName" -> Optional.ofNullable(state.storage.get(args[0]))
                        .filter(outboxEvent -> outboxEvent.getServiceName().equals(args[1]));
                case "save", "saveAndFlush" -> persist(state, (OutboxEvent) args[0]);
                case "toString" -> "BillingOutboxEventRepositoryTestProxy";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                default -> throw new UnsupportedOperationException("Unsupported repository method: " + method.getName());
            };
        }
    }
}
