package com.campusenroll.enrollmentservice.enrollment.dto;

import com.campusenroll.enrollmentservice.enrollment.EnrollmentStatus;
import java.time.OffsetDateTime;

public record EnrollmentResponse(
        Long id,
        Long studentId,
        Long sectionId,
        EnrollmentStatus status,
        OffsetDateTime enrolledAt) {
}
