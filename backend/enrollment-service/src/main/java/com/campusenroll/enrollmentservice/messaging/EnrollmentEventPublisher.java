package com.campusenroll.enrollmentservice.messaging;

import com.campusenroll.enrollmentservice.enrollment.Enrollment;

public interface EnrollmentEventPublisher {

    void publishEnrollmentCreated(Enrollment enrollment);
}
