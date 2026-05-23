package com.campusenroll.enrollmentservice.messaging;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BillingStatusChangedEvent(
        UUID eventId,
        Long billingId,
        Long enrollmentId,
        String previousStatus,
        String newStatus,
        OffsetDateTime occurredAt) {}
