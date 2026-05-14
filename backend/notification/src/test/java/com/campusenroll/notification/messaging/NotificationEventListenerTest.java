package com.campusenroll.notification.messaging;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

class NotificationEventListenerTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void shouldRecordEnrollmentCreatedEvidence() throws Exception {
        NotificationEventEvidenceStore evidenceStore = new NotificationEventEvidenceStore();
        NotificationEventListener listener =
                new NotificationEventListener(objectMapper, evidenceStore, "enrollment.created", "billing.status.changed");

        EnrollmentCreatedEvent event =
                new EnrollmentCreatedEvent(UUID.randomUUID(), 10L, 100L, 200L, "ENROLLED", OffsetDateTime.now());

        listener.handleMessage(message("enrollment.created", objectMapper.writeValueAsBytes(event)));

        assertThat(evidenceStore.snapshot()).hasSize(1);
        assertThat(evidenceStore.snapshot().get(0)).contains("enrollmentId=10");
        assertThat(evidenceStore.snapshot().get(0)).contains("studentId=100");
    }

    @Test
    void shouldRecordBillingStatusChangedEvidence() throws Exception {
        NotificationEventEvidenceStore evidenceStore = new NotificationEventEvidenceStore();
        NotificationEventListener listener =
                new NotificationEventListener(objectMapper, evidenceStore, "enrollment.created", "billing.status.changed");

        BillingStatusChangedEvent event = new BillingStatusChangedEvent(
                UUID.randomUUID(), 20L, 10L, "PENDING", "PAID", OffsetDateTime.now());

        listener.handleMessage(message("billing.status.changed", objectMapper.writeValueAsBytes(event)));

        assertThat(evidenceStore.snapshot()).hasSize(1);
        assertThat(evidenceStore.snapshot().get(0)).contains("billingId=20");
        assertThat(evidenceStore.snapshot().get(0)).contains("newStatus=PAID");
    }

    @Test
    void shouldIgnoreUnsupportedRoutingKeys() {
        NotificationEventEvidenceStore evidenceStore = new NotificationEventEvidenceStore();
        NotificationEventListener listener =
                new NotificationEventListener(objectMapper, evidenceStore, "enrollment.created", "billing.status.changed");

        listener.handleMessage(message("unknown.route", "{}".getBytes()));

        assertThat(evidenceStore.snapshot()).isEmpty();
    }

    private Message message(String routingKey, byte[] body) {
        MessageProperties properties = new MessageProperties();
        properties.setReceivedRoutingKey(routingKey);
        return new Message(body, properties);
    }
}
