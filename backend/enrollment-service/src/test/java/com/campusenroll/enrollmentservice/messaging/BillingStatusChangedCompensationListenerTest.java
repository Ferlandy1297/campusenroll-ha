package com.campusenroll.enrollmentservice.messaging;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.campusenroll.enrollmentservice.enrollment.EnrollmentCompensationResult;
import com.campusenroll.enrollmentservice.enrollment.EnrollmentService;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class BillingStatusChangedCompensationListenerTest {

    @Test
    void shouldForwardCancelledBillingEventToEnrollmentService() {
        EnrollmentService enrollmentService = mock(EnrollmentService.class);
        Mockito.when(enrollmentService.compensateEnrollmentForCancelledBilling(10L))
                .thenReturn(EnrollmentCompensationResult.COMPENSATED);
        BillingStatusChangedCompensationListener listener =
                new BillingStatusChangedCompensationListener(enrollmentService);

        listener.handleBillingStatusChanged(event("CANCELLED", 10L));

        verify(enrollmentService).compensateEnrollmentForCancelledBilling(10L);
    }

    @Test
    void shouldIgnorePaidAndPendingBillingEvents() {
        EnrollmentService enrollmentService = mock(EnrollmentService.class);
        BillingStatusChangedCompensationListener listener =
                new BillingStatusChangedCompensationListener(enrollmentService);

        listener.handleBillingStatusChanged(event("PAID", 10L));
        listener.handleBillingStatusChanged(event("PENDING", 10L));

        verifyNoInteractions(enrollmentService);
    }

    private BillingStatusChangedEvent event(String newStatus, Long enrollmentId) {
        return new BillingStatusChangedEvent(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                20L,
                enrollmentId,
                "PENDING",
                newStatus,
                OffsetDateTime.parse("2026-05-22T10:15:30Z"));
    }
}
