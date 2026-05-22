package com.campusenroll.enrollmentservice.messaging;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.core.AmqpTemplate;

class RabbitEnrollmentEventPublisherTest {

    @Test
    void shouldPublishEnrollmentCreatedEvent() {
        AmqpTemplate rabbitTemplate = org.mockito.Mockito.mock(AmqpTemplate.class);
        RabbitEnrollmentEventPublisher publisher =
                new RabbitEnrollmentEventPublisher(rabbitTemplate, "campusenroll.events", "enrollment.created");

        EnrollmentCreatedEvent expectedEvent = event();
        publisher.publishEnrollmentCreated(expectedEvent);

        ArgumentCaptor<EnrollmentCreatedEvent> eventCaptor = ArgumentCaptor.forClass(EnrollmentCreatedEvent.class);
        verify(rabbitTemplate)
                .convertAndSend(eq("campusenroll.events"), eq("enrollment.created"), eventCaptor.capture());
        EnrollmentCreatedEvent event = eventCaptor.getValue();
        org.assertj.core.api.Assertions.assertThat(event).isEqualTo(expectedEvent);
    }

    @Test
    void shouldRethrowPublishFailures() {
        AmqpTemplate rabbitTemplate = org.mockito.Mockito.mock(AmqpTemplate.class);
        doThrow(new AmqpConnectException(new RuntimeException("offline")))
                .when(rabbitTemplate)
                .convertAndSend(any(String.class), any(String.class), any(Object.class));

        RabbitEnrollmentEventPublisher publisher =
                new RabbitEnrollmentEventPublisher(rabbitTemplate, "campusenroll.events", "enrollment.created");

        assertThatThrownBy(() -> publisher.publishEnrollmentCreated(event()))
                .isInstanceOf(AmqpConnectException.class);

        verify(rabbitTemplate).convertAndSend(any(String.class), any(String.class), any(Object.class));
    }

    private EnrollmentCreatedEvent event() {
        return new EnrollmentCreatedEvent(
                java.util.UUID.fromString("11111111-1111-1111-1111-111111111111"),
                10L,
                100L,
                200L,
                "ENROLLED",
                java.time.OffsetDateTime.parse("2026-05-21T10:15:30Z"));
    }
}
