package com.campusenroll.enrollmentservice.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
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
    Queue enrollmentCompensationQueue(@Value("${app.messaging.enrollment-compensation-queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    Binding billingStatusChangedCompensationBinding(
            Queue enrollmentCompensationQueue,
            TopicExchange campusEventsExchange,
            @Value("${app.messaging.billing-status-changed-routing-key}") String routingKey) {
        return BindingBuilder.bind(enrollmentCompensationQueue).to(campusEventsExchange).with(routingKey);
    }

    @Bean
    MessageConverter rabbitMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
}
