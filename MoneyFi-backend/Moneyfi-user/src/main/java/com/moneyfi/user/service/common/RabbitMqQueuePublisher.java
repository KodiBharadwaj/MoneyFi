package com.moneyfi.user.service.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.moneyfi.user.service.common.dto.internal.NotificationQueueDto;
import com.moneyfi.user.util.constants.StringConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitMqQueuePublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Value("${rabbitmq.routing-key}")
    private String routingKey;

    public void publish(NotificationQueueDto notificationQueueDto) throws JsonProcessingException {
        rabbitTemplate.convertAndSend(exchange, routingKey, StringConstants.objectMapper.writeValueAsString(notificationQueueDto));
    }
}
