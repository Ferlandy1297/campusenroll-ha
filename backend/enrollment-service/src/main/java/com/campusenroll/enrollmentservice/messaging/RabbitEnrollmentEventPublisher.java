package com.campusenroll.enrollmentservice.messaging;

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
    public void publishEnrollmentCreated(EnrollmentCreatedEvent event) {
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
                    event.enrollmentId(),
                    enrollmentCreatedRoutingKey,
                    ex.getMessage());
            throw ex;
        }
    }
}
