package com.moneyfi.user.service.common;

import com.moneyfi.user.service.common.dto.internal.NotificationQueueDto;
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
