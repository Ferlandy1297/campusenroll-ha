package com.campusenroll.enrollmentservice.messaging;

import com.campusenroll.enrollmentservice.enrollment.Enrollment;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RabbitEnrollmentEventPublisher implements EnrollmentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RabbitEnrollmentEventPublisher.class);

    private final AmqpTemplate amqpTemplate;
    private final String exchange;
    private final String enrollmentCreatedRoutingKey;

    public RabbitEnrollmentEventPublisher(
            AmqpTemplate amqpTemplate,
            @Value("${app.messaging.exchange}") String exchange,
            @Value("${app.messaging.enrollment-created-routing-key}") String enrollmentCreatedRoutingKey) {
        this.amqpTemplate = amqpTemplate;
        this.exchange = exchange;
        this.enrollmentCreatedRoutingKey = enrollmentCreatedRoutingKey;
    }

    @Override
    public void publishEnrollmentCreated(Enrollment enrollment) {
        EnrollmentCreatedEvent event = new EnrollmentCreatedEvent(
                UUID.randomUUID(),
                enrollment.getId(),
                enrollment.getStudentId(),
                enrollment.getSectionId(),
                enrollment.getStatus().name(),
                OffsetDateTime.now());

        try {
            amqpTemplate.convertAndSend(exchange, enrollmentCreatedRoutingKey, event);
            log.info(
                    "Published EnrollmentCreatedEvent eventId={} enrollmentId={} routingKey={}",
                    event.eventId(),
                    event.enrollmentId(),
                    enrollmentCreatedRoutingKey);
        } catch (AmqpException ex) {
            log.warn(
                    "Failed to publish EnrollmentCreatedEvent enrollmentId={} routingKey={}: {}",
                    enrollment.getId(),
                    enrollmentCreatedRoutingKey,
                    ex.getMessage());
        }
    }
}
