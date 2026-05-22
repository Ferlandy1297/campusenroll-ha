package com.campusenroll.enrollmentservice.outbox;

import static org.assertj.core.api.Assertions.assertThat;

import com.campusenroll.enrollmentservice.messaging.EnrollmentCreatedEvent;
import com.campusenroll.enrollmentservice.messaging.EnrollmentEventPublisher;
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

class EnrollmentOutboxAttemptServiceTest {

    @Test
    void shouldPublishPendingEnrollmentOutboxEventAndMarkItPublished() throws Exception {
        RepositoryState state = new RepositoryState();
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        EnrollmentCreatedEvent event = new EnrollmentCreatedEvent(
                UUID.randomUUID(),
                10L,
                100L,
                200L,
                "ENROLLED",
                OffsetDateTime.parse("2026-05-21T10:15:30Z"));
        OutboxEvent outboxEvent = outboxEvent(objectMapper.writeValueAsString(event));
        persist(state, outboxEvent);

        RecordingEnrollmentEventPublisher publisher = new RecordingEnrollmentEventPublisher();
        EnrollmentOutboxAttemptService attemptService =
                new EnrollmentOutboxAttemptService(repository(state), publisher, objectMapper, 5);

        attemptService.publishPendingEvent(outboxEvent.getId());

        assertThat(publisher.publishedEvents).hasSize(1);
        assertThat(publisher.publishedEvents.get(0).enrollmentId()).isEqualTo(10L);
        assertThat(outboxEvent.getStatus()).isEqualTo(OutboxEventStatus.PUBLISHED);
        assertThat(outboxEvent.getPublishedAt()).isNotNull();
        assertThat(outboxEvent.getLastError()).isNull();
    }

    @Test
    void shouldMarkEnrollmentOutboxEventAsFailedAfterMaxAttempts() {
        RepositoryState state = new RepositoryState();
        OutboxEvent outboxEvent = outboxEvent("{\"ignored\":true}");
        outboxEvent.setAttempts(4);
        persist(state, outboxEvent);

        EnrollmentOutboxAttemptService attemptService = new EnrollmentOutboxAttemptService(
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
        outboxEvent.setServiceName("enrollment-service");
        outboxEvent.setAggregateType("enrollment");
        outboxEvent.setAggregateId(10L);
        outboxEvent.setEventType("EnrollmentCreatedEvent");
        outboxEvent.setRoutingKey("enrollment.created");
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

    private static final class RecordingEnrollmentEventPublisher implements EnrollmentEventPublisher {
        private final List<EnrollmentCreatedEvent> publishedEvents = new ArrayList<>();

        @Override
        public void publishEnrollmentCreated(EnrollmentCreatedEvent event) {
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
                case "toString" -> "EnrollmentOutboxEventRepositoryTestProxy";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                default -> throw new UnsupportedOperationException("Unsupported repository method: " + method.getName());
            };
        }
    }
}
