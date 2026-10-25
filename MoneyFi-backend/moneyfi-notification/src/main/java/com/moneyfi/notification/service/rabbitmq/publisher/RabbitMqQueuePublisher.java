package com.moneyfi.notification.service.rabbitmq.publisher;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitMqQueuePublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.response.routing-key}")
    private String routingKey;

    @Autowired
    private ConnectionFactory connectionFactory;

    @PostConstruct
    public void check() {
        System.out.println("ConnectionFactory = " + connectionFactory);
    }

    public void publishMessage(String message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }
}
