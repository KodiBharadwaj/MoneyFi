package com.moneyfi.user.service.general.rabbitmq.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.moneyfi.user.service.user.dto.internal.NotificationQueueDto;
import com.moneyfi.user.util.constants.StringConstants;
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

    @Value("${rabbitmq.request.routing-key}")
    private String routingKey;

    @Autowired
    private ConnectionFactory connectionFactory;

    @PostConstruct
    public void check() {
        System.out.println("ConnectionFactory = " + connectionFactory);
    }

    public void publishMessage(NotificationQueueDto notificationQueueDto) throws JsonProcessingException {
        rabbitTemplate.convertAndSend(exchange, routingKey, StringConstants.objectMapper.writeValueAsString(notificationQueueDto));
    }
}
