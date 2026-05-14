package com.campusenroll.notification.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    private final ObjectMapper objectMapper;
    private final NotificationEventEvidenceStore evidenceStore;
    private final String enrollmentCreatedRoutingKey;
    private final String billingStatusChangedRoutingKey;

    public NotificationEventListener(
            ObjectMapper objectMapper,
            NotificationEventEvidenceStore evidenceStore,
            @Value("${app.messaging.enrollment-created-routing-key}") String enrollmentCreatedRoutingKey,
            @Value("${app.messaging.billing-status-changed-routing-key}") String billingStatusChangedRoutingKey) {
        this.objectMapper = objectMapper;
        this.evidenceStore = evidenceStore;
        this.enrollmentCreatedRoutingKey = enrollmentCreatedRoutingKey;
        this.billingStatusChangedRoutingKey = billingStatusChangedRoutingKey;
    }

    @RabbitListener(queues = "${app.messaging.notification-queue}")
    public void handleMessage(Message message) {
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();

        try {
            if (enrollmentCreatedRoutingKey.equals(routingKey)) {
                handleEnrollmentCreated(objectMapper.readValue(message.getBody(), EnrollmentCreatedEvent.class));
                return;
            }

            if (billingStatusChangedRoutingKey.equals(routingKey)) {
                handleBillingStatusChanged(objectMapper.readValue(message.getBody(), BillingStatusChangedEvent.class));
                return;
            }

            log.warn("Notification received unsupported routingKey={}", routingKey);
        } catch (IOException ex) {
            log.error("Notification failed to deserialize event for routingKey={}: {}", routingKey, ex.getMessage());
        }
    }

    void handleEnrollmentCreated(EnrollmentCreatedEvent event) {
        String evidence = "Enrollment created event received: enrollmentId=%d studentId=%d sectionId=%d status=%s eventId=%s"
                .formatted(event.enrollmentId(), event.studentId(), event.sectionId(), event.status(), event.eventId());
        evidenceStore.record(evidence);
        log.info(evidence);
    }

    void handleBillingStatusChanged(BillingStatusChangedEvent event) {
        String evidence =
                "Billing status changed event received: billingId=%d enrollmentId=%d previousStatus=%s newStatus=%s eventId=%s"
                        .formatted(
                                event.billingId(),
                                event.enrollmentId(),
                                event.previousStatus(),
                                event.newStatus(),
                                event.eventId());
        evidenceStore.record(evidence);
        log.info(evidence);
    }
}
