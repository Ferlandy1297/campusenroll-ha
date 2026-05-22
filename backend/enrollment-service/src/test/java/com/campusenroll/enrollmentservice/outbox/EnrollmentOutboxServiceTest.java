package com.campusenroll.enrollmentservice.outbox;

import static org.assertj.core.api.Assertions.assertThat;

import com.campusenroll.enrollmentservice.enrollment.Enrollment;
import com.campusenroll.enrollmentservice.enrollment.EnrollmentStatus;
import com.campusenroll.enrollmentservice.messaging.EnrollmentCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EnrollmentOutboxServiceTest {

    @Test
    void shouldPersistPendingEnrollmentCreatedOutboxRow() throws Exception {
        RepositoryState state = new RepositoryState();
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        EnrollmentOutboxService outboxService =
                new EnrollmentOutboxService(repository(state), objectMapper, "enrollment.created");

        Enrollment enrollment = new Enrollment();
        enrollment.setId(10L);
        enrollment.setStudentId(100L);
        enrollment.setSectionId(200L);
        enrollment.setStatus(EnrollmentStatus.ENROLLED);
        enrollment.setEnrolledAt(OffsetDateTime.parse("2026-05-21T10:15:30Z"));

        outboxService.enqueueEnrollmentCreated(enrollment);

        assertThat(state.storage).hasSize(1);
        OutboxEvent storedEvent = state.storage.get(1L);
        assertThat(storedEvent.getServiceName()).isEqualTo("enrollment-service");
        assertThat(storedEvent.getAggregateType()).isEqualTo("enrollment");
        assertThat(storedEvent.getAggregateId()).isEqualTo(10L);
        assertThat(storedEvent.getEventType()).isEqualTo("EnrollmentCreatedEvent");
        assertThat(storedEvent.getRoutingKey()).isEqualTo("enrollment.created");
        assertThat(storedEvent.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(storedEvent.getAttempts()).isZero();
        assertThat(storedEvent.getCreatedAt()).isNotNull();
        assertThat(storedEvent.getPublishedAt()).isNull();

        EnrollmentCreatedEvent payload =
                objectMapper.readValue(storedEvent.getPayload(), EnrollmentCreatedEvent.class);
        assertThat(payload.enrollmentId()).isEqualTo(10L);
        assertThat(payload.studentId()).isEqualTo(100L);
        assertThat(payload.sectionId()).isEqualTo(200L);
        assertThat(payload.status()).isEqualTo("ENROLLED");
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
                case "toString" -> "EnrollmentOutboxEventRepositoryTestProxy";
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
