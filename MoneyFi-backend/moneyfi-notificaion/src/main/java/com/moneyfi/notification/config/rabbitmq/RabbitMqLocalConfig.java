package com.moneyfi.notification.config.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static com.moneyfi.notification.util.constants.StringConstants.LOCAL_PROFILE_RABBIT_MQ;

@Configuration
@Profile(LOCAL_PROFILE_RABBIT_MQ)
public class RabbitMqLocalConfig {
    @Value("${rabbitmq.queue}")
    private String queue;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-key}")
    private String routingKey;

    @Bean
    public Queue passwordChangedQueue() {
        return new Queue(queue, true);
    }

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(exchange);
    }

    @Bean
    public Binding binding(Queue passwordChangedQueue, TopicExchange notificationExchange) {
        return BindingBuilder
                .bind(passwordChangedQueue)
                .to(notificationExchange)
                .with(routingKey);
    }
}
