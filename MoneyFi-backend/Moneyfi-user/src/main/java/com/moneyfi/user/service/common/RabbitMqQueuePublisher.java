package com.moneyfi.user.service.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RabbitMqQueuePublisher {

    private final RabbitMqQueuePublisher rabbitMqQueuePublisher;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-key}")
    private String routingKey;

    public RabbitMqQueuePublisher(RabbitMqQueuePublisher rabbitMqQueuePublisher) {
        this.rabbitMqQueuePublisher = rabbitMqQueuePublisher;
    }

//    public void publishPasswordChangedEvent(PasswordChangedEmailDto dto) {
//        rabbitTemplate.convertAndSend(exchange, routingKey, dto);
//    }
}
