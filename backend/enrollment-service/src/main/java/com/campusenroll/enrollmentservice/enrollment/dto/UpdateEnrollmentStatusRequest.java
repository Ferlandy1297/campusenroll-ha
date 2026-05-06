package com.campusenroll.enrollmentservice.enrollment.dto;

import com.campusenroll.enrollmentservice.enrollment.EnrollmentStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateEnrollmentStatusRequest {

    @NotNull(message = "status is required")
    private EnrollmentStatus status;

    public EnrollmentStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }
}
