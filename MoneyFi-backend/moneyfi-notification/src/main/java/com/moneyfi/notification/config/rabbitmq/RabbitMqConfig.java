package com.moneyfi.notification.config.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.communication.queue-provider", havingValue = "rabbitmq")
public class RabbitMqConfig {

    @Value("${rabbitmq.exchange}")
    private String exchangeName;

    @Value("${rabbitmq.exchange-type:topic}")
    private String exchangeType;

    @Value("${rabbitmq.request.queue}")
    private String requestQueue;

    @Value("${rabbitmq.request.routing-key}")
    private String requestRoutingKey;

    @Value("${rabbitmq.response.queue}")
    private String responseQueue;

    @Value("${rabbitmq.response.routing-key}")
    private String responseRoutingKey;

    @Bean
    public Exchange exchange() {
        return switch (exchangeType.toLowerCase()) {
            case "direct" -> new DirectExchange(exchangeName);
            case "fanout" -> new FanoutExchange(exchangeName);
            default -> new TopicExchange(exchangeName);
        };
    }

    @Bean
    public Queue requestQueue() {
        return new Queue(requestQueue);
    }

    @Bean
    public Queue responseQueue() {
        return new Queue(responseQueue);
    }

    @Bean
    public Binding requestBinding(Exchange exchange) {

        if (exchange instanceof DirectExchange directExchange) {
            return BindingBuilder.bind(requestQueue())
                    .to(directExchange)
                    .with(requestRoutingKey);
        }

        if (exchange instanceof TopicExchange topicExchange) {
            return BindingBuilder.bind(requestQueue())
                    .to(topicExchange)
                    .with(requestRoutingKey);
        }

        if (exchange instanceof FanoutExchange fanoutExchange) {
            return BindingBuilder.bind(requestQueue())
                    .to(fanoutExchange);
        }

        throw new IllegalStateException("Unsupported exchange type: " + exchangeType);
    }

    @Bean
    public Binding responseBinding(Exchange exchange) {

        if (exchange instanceof DirectExchange directExchange) {
            return BindingBuilder.bind(responseQueue())
                    .to(directExchange)
                    .with(responseRoutingKey);
        }

        if (exchange instanceof TopicExchange topicExchange) {
            return BindingBuilder.bind(responseQueue())
                    .to(topicExchange)
                    .with(responseRoutingKey);
        }

        if (exchange instanceof FanoutExchange fanoutExchange) {
            return BindingBuilder.bind(responseQueue())
                    .to(fanoutExchange);
        }

        throw new IllegalStateException("Unsupported exchange type: " + exchangeType);
    }
}