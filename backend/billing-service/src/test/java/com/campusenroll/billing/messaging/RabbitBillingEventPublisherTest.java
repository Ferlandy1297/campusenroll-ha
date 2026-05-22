package com.campusenroll.billing.messaging;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
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

        BillingStatusChangedEvent expectedEvent = event();
        publisher.publishBillingStatusChanged(expectedEvent);

        ArgumentCaptor<BillingStatusChangedEvent> eventCaptor = ArgumentCaptor.forClass(BillingStatusChangedEvent.class);
        verify(rabbitTemplate)
                .convertAndSend(eq("campusenroll.events"), eq("billing.status.changed"), eventCaptor.capture());
        BillingStatusChangedEvent event = eventCaptor.getValue();
        org.assertj.core.api.Assertions.assertThat(event).isEqualTo(expectedEvent);
    }

    @Test
    void shouldRethrowPublishFailures() {
        AmqpTemplate rabbitTemplate = org.mockito.Mockito.mock(AmqpTemplate.class);
        doThrow(new AmqpConnectException(new RuntimeException("offline")))
                .when(rabbitTemplate)
                .convertAndSend(any(String.class), any(String.class), any(Object.class));

        RabbitBillingEventPublisher publisher =
                new RabbitBillingEventPublisher(rabbitTemplate, "campusenroll.events", "billing.status.changed");

        assertThatThrownBy(() -> publisher.publishBillingStatusChanged(event()))
                .isInstanceOf(AmqpConnectException.class);

        verify(rabbitTemplate).convertAndSend(any(String.class), any(String.class), any(Object.class));
    }

    private BillingStatusChangedEvent event() {
        return new BillingStatusChangedEvent(
                java.util.UUID.fromString("22222222-2222-2222-2222-222222222222"),
                20L,
                100L,
                "PENDING",
                "PAID",
                java.time.OffsetDateTime.parse("2026-05-21T10:15:30Z"));
    }
}
