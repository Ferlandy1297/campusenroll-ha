package com.campusenroll.notification.messaging;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EnrollmentCreatedEvent(
        UUID eventId,
        Long enrollmentId,
        Long studentId,
        Long sectionId,
        String status,
        OffsetDateTime occurredAt) {}
