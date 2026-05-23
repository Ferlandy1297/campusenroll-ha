package com.campusenroll.enrollmentservice.messaging;

import com.campusenroll.enrollmentservice.enrollment.EnrollmentCompensationResult;
import com.campusenroll.enrollmentservice.enrollment.EnrollmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class BillingStatusChangedCompensationListener {

    private static final Logger log = LoggerFactory.getLogger(BillingStatusChangedCompensationListener.class);
    private static final String CANCELLED_STATUS = "CANCELLED";

    private final EnrollmentService enrollmentService;

    public BillingStatusChangedCompensationListener(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @RabbitListener(queues = "${app.messaging.enrollment-compensation-queue}")
    public void handleBillingStatusChanged(BillingStatusChangedEvent event) {
        if (event == null) {
            log.warn("Ignoring null billing.status.changed event");
            return;
        }

        if (!CANCELLED_STATUS.equals(event.newStatus())) {
            log.info(
                    "Ignoring billing.status.changed eventId={} billingId={} enrollmentId={} newStatus={}",
                    event.eventId(),
                    event.billingId(),
                    event.enrollmentId(),
                    event.newStatus());
            return;
        }

        if (event.enrollmentId() == null) {
            log.warn(
                    "Ignoring cancelled billing eventId={} billingId={} because enrollmentId is null",
                    event.eventId(),
                    event.billingId());
            return;
        }

        EnrollmentCompensationResult result =
                enrollmentService.compensateEnrollmentForCancelledBilling(event.enrollmentId());

        log.info(
                "Processed billing cancellation compensation eventId={} billingId={} enrollmentId={} result={}",
                event.eventId(),
                event.billingId(),
                event.enrollmentId(),
                result);
    }
}
