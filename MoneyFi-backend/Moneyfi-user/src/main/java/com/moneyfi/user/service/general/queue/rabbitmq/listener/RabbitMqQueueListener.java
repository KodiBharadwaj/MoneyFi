package com.moneyfi.user.service.general.queue.rabbitmq.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.communication.queue-provider", havingValue = "rabbitmq")
public class RabbitMqQueueListener {

    @RabbitListener(queues = "${rabbitmq.response.queue}")
    public void handleRabbitQueueEventListener(String message) {
        log.info("checking listener input: {}", message);
    }
}