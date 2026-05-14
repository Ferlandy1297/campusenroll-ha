package com.campusenroll.billing.messaging;

import com.campusenroll.billing.billing.Billing;
import com.campusenroll.billing.billing.BillingStatus;

public interface BillingEventPublisher {

    void publishBillingStatusChanged(Billing billing, BillingStatus previousStatus, BillingStatus newStatus);
}
