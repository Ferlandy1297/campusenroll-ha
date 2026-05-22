package com.campusenroll.enrollmentservice.messaging;

public interface EnrollmentEventPublisher {

    void publishEnrollmentCreated(EnrollmentCreatedEvent event);
}
