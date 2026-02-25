package com.moneyfi.apigateway.service.general;

import com.moneyfi.apigateway.service.general.dto.NotificationQueueDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EventPublisherService {

    @Autowired
    private RabbitMqQueuePublisher rabbitMqQueuePublisher;

    @Async
    @EventListener
    @Order(1)
    public void sendUserNotification(NotificationQueueDto notificationQueueDto) {
        try {
            rabbitMqQueuePublisher.publish(notificationQueueDto);
        } catch (Exception e) {
            log.warn("Error while send user notification : {}", e.getMessage());
        }
    }
}
