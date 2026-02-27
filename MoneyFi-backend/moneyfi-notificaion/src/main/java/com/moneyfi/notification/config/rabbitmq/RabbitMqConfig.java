package com.moneyfi.notification.config.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local-rabbitmq")
public class RabbitMqConfig {

    @Value("${rabbitmq.queue}")
    private String queueName;

    @Value("${rabbitmq.exchange}")
    private String exchangeName;

    @Value("${rabbitmq.routing-key}")
    private String routingKey;

    @Bean
    public Queue passwordChangedQueue() {
        return new Queue(queueName, true);
    }

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Binding binding(Queue passwordChangedQueue, TopicExchange notificationExchange) {
        return BindingBuilder
                .bind(passwordChangedQueue)
                .to(notificationExchange)
                .with(routingKey);
    }
}
