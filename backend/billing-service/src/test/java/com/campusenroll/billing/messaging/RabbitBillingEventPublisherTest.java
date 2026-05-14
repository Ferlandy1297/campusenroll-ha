package com.campusenroll.billing.messaging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.campusenroll.billing.billing.Billing;
import com.campusenroll.billing.billing.BillingStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.core.AmqpTemplate;

class RabbitBillingEventPublisherTest {

    @Test
    void shouldPublishBillingStatusChangedEvent() {
        AmqpTemplate rabbitTemplate = org.mockito.Mockito.mock(AmqpTemplate.class);
        RabbitBillingEventPublisher publisher =
                new RabbitBillingEventPublisher(rabbitTemplate, "campusenroll.events", "billing.status.changed");

        publisher.publishBillingStatusChanged(billing(), BillingStatus.PENDING, BillingStatus.PAID);

        ArgumentCaptor<BillingStatusChangedEvent> eventCaptor = ArgumentCaptor.forClass(BillingStatusChangedEvent.class);
        verify(rabbitTemplate)
                .convertAndSend(eq("campusenroll.events"), eq("billing.status.changed"), eventCaptor.capture());
        BillingStatusChangedEvent event = eventCaptor.getValue();
        org.assertj.core.api.Assertions.assertThat(event.billingId()).isEqualTo(20L);
        org.assertj.core.api.Assertions.assertThat(event.enrollmentId()).isEqualTo(100L);
        org.assertj.core.api.Assertions.assertThat(event.previousStatus()).isEqualTo("PENDING");
        org.assertj.core.api.Assertions.assertThat(event.newStatus()).isEqualTo("PAID");
        org.assertj.core.api.Assertions.assertThat(event.eventId()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(event.occurredAt()).isNotNull();
    }

    @Test
    void shouldSwallowPublishFailures() {
        AmqpTemplate rabbitTemplate = org.mockito.Mockito.mock(AmqpTemplate.class);
        doThrow(new AmqpConnectException(new RuntimeException("offline")))
                .when(rabbitTemplate)
                .convertAndSend(any(String.class), any(String.class), any(Object.class));

        RabbitBillingEventPublisher publisher =
                new RabbitBillingEventPublisher(rabbitTemplate, "campusenroll.events", "billing.status.changed");

        publisher.publishBillingStatusChanged(billing(), BillingStatus.PENDING, BillingStatus.PAID);

        verify(rabbitTemplate).convertAndSend(any(String.class), any(String.class), any(Object.class));
        verifyNoMoreInteractions(rabbitTemplate);
    }

    private Billing billing() {
        Billing billing = new Billing();
        billing.setId(20L);
        billing.setEnrollmentId(100L);
        billing.setAmount(new BigDecimal("150.75"));
        billing.setCurrency("USD");
        billing.setStatus(BillingStatus.PAID);
        billing.setCreatedAt(OffsetDateTime.now());
        return billing;
    }
}
