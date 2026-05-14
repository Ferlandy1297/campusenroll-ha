package com.campusenroll.enrollmentservice.messaging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.campusenroll.enrollmentservice.enrollment.Enrollment;
import com.campusenroll.enrollmentservice.enrollment.EnrollmentStatus;
import java.time.OffsetDateTime;
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

        publisher.publishEnrollmentCreated(enrollment());

        ArgumentCaptor<EnrollmentCreatedEvent> eventCaptor = ArgumentCaptor.forClass(EnrollmentCreatedEvent.class);
        verify(rabbitTemplate)
                .convertAndSend(eq("campusenroll.events"), eq("enrollment.created"), eventCaptor.capture());
        EnrollmentCreatedEvent event = eventCaptor.getValue();
        org.assertj.core.api.Assertions.assertThat(event.enrollmentId()).isEqualTo(10L);
        org.assertj.core.api.Assertions.assertThat(event.studentId()).isEqualTo(100L);
        org.assertj.core.api.Assertions.assertThat(event.sectionId()).isEqualTo(200L);
        org.assertj.core.api.Assertions.assertThat(event.status()).isEqualTo("ENROLLED");
        org.assertj.core.api.Assertions.assertThat(event.eventId()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(event.occurredAt()).isNotNull();
    }

    @Test
    void shouldSwallowPublishFailures() {
        AmqpTemplate rabbitTemplate = org.mockito.Mockito.mock(AmqpTemplate.class);
        doThrow(new AmqpConnectException(new RuntimeException("offline")))
                .when(rabbitTemplate)
                .convertAndSend(any(String.class), any(String.class), any(Object.class));

        RabbitEnrollmentEventPublisher publisher =
                new RabbitEnrollmentEventPublisher(rabbitTemplate, "campusenroll.events", "enrollment.created");

        publisher.publishEnrollmentCreated(enrollment());

        verify(rabbitTemplate).convertAndSend(any(String.class), any(String.class), any(Object.class));
        verifyNoMoreInteractions(rabbitTemplate);
    }

    private Enrollment enrollment() {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(10L);
        enrollment.setStudentId(100L);
        enrollment.setSectionId(200L);
        enrollment.setStatus(EnrollmentStatus.ENROLLED);
        enrollment.setEnrolledAt(OffsetDateTime.now());
        return enrollment;
    }
}
