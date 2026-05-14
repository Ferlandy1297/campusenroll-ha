package com.campusenroll.notification.messaging;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BillingStatusChangedEvent(
        UUID eventId,
        Long billingId,
        Long enrollmentId,
        String previousStatus,
        String newStatus,
        OffsetDateTime occurredAt) {}
