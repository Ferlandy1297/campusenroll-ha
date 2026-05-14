package com.campusenroll.notification.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfiguration {

    @Bean
    TopicExchange campusEventsExchange(@Value("${app.messaging.exchange}") String exchangeName) {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    Queue notificationEventsQueue(@Value("${app.messaging.notification-queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    Binding enrollmentCreatedBinding(
            Queue notificationEventsQueue,
            TopicExchange campusEventsExchange,
            @Value("${app.messaging.enrollment-created-routing-key}") String routingKey) {
        return BindingBuilder.bind(notificationEventsQueue).to(campusEventsExchange).with(routingKey);
    }

    @Bean
    Binding billingStatusChangedBinding(
            Queue notificationEventsQueue,
            TopicExchange campusEventsExchange,
            @Value("${app.messaging.billing-status-changed-routing-key}") String routingKey) {
        return BindingBuilder.bind(notificationEventsQueue).to(campusEventsExchange).with(routingKey);
    }

    @Bean
    RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
}
