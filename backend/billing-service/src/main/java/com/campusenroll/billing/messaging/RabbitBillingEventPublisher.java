package com.campusenroll.billing.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RabbitBillingEventPublisher implements BillingEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RabbitBillingEventPublisher.class);

    private final AmqpTemplate amqpTemplate;
    private final String exchange;
    private final String billingStatusChangedRoutingKey;

    public RabbitBillingEventPublisher(
            AmqpTemplate amqpTemplate,
            @Value("${app.messaging.exchange}") String exchange,
            @Value("${app.messaging.billing-status-changed-routing-key}") String billingStatusChangedRoutingKey) {
        this.amqpTemplate = amqpTemplate;
        this.exchange = exchange;
        this.billingStatusChangedRoutingKey = billingStatusChangedRoutingKey;
    }

    @Override
    public void publishBillingStatusChanged(BillingStatusChangedEvent event) {
        try {
            amqpTemplate.convertAndSend(exchange, billingStatusChangedRoutingKey, event);
            log.info(
                    "Published BillingStatusChangedEvent eventId={} billingId={} routingKey={}",
                    event.eventId(),
                    event.billingId(),
                    billingStatusChangedRoutingKey);
        } catch (AmqpException ex) {
            log.warn(
                    "Failed to publish BillingStatusChangedEvent billingId={} routingKey={}: {}",
                    event.billingId(),
                    billingStatusChangedRoutingKey,
                    ex.getMessage());
            throw ex;
        }
    }
}
