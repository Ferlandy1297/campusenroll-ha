package com.campusenroll.billing.messaging;

public interface BillingEventPublisher {

    void publishBillingStatusChanged(BillingStatusChangedEvent event);
}
