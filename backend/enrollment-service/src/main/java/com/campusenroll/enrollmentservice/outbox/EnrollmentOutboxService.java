package com.campusenroll.enrollmentservice.outbox;

import com.campusenroll.enrollmentservice.enrollment.Enrollment;
import com.campusenroll.enrollmentservice.messaging.EnrollmentCreatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EnrollmentOutboxService {

    static final String SERVICE_NAME = "enrollment-service";
    private static final String AGGREGATE_TYPE = "enrollment";
    private static final String EVENT_TYPE = "EnrollmentCreatedEvent";

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final String enrollmentCreatedRoutingKey;

    public EnrollmentOutboxService(
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper,
            @Value("${app.messaging.enrollment-created-routing-key}") String enrollmentCreatedRoutingKey) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
        this.enrollmentCreatedRoutingKey = enrollmentCreatedRoutingKey;
    }

    public void enqueueEnrollmentCreated(Enrollment enrollment) {
        EnrollmentCreatedEvent event = new EnrollmentCreatedEvent(
                UUID.randomUUID(),
                enrollment.getId(),
                enrollment.getStudentId(),
                enrollment.getSectionId(),
                enrollment.getStatus().name(),
                OffsetDateTime.now());

        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setServiceName(SERVICE_NAME);
        outboxEvent.setAggregateType(AGGREGATE_TYPE);
        outboxEvent.setAggregateId(enrollment.getId());
        outboxEvent.setEventType(EVENT_TYPE);
        outboxEvent.setRoutingKey(enrollmentCreatedRoutingKey);
        outboxEvent.setPayload(serialize(event));
        outboxEvent.setStatus(OutboxEventStatus.PENDING);
        outboxEvent.setAttempts(0);
        outboxEvent.setCreatedAt(OffsetDateTime.now());
        outboxEventRepository.save(outboxEvent);
    }

    private String serialize(EnrollmentCreatedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize enrollment outbox payload", ex);
        }
    }
}
