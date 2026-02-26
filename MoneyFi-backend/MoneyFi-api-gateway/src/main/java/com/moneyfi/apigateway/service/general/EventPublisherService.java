package com.moneyfi.apigateway.service.general;

import com.moneyfi.apigateway.service.general.dto.NotificationQueueDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisherService {

    @Value("${spring.profiles.active:}")
    private String activeProfile;

    private final RabbitMqQueuePublisher rabbitMqQueuePublisher;
    private final ArtemisQueueProducer artemisQueueProducer;

    @Async
    @EventListener
    @Order(1)
    public void sendUserNotification(NotificationQueueDto notificationQueueDto) {
        try {
            if ("local".equalsIgnoreCase(activeProfile)) {
                artemisQueueProducer.sendMessage(notificationQueueDto);
            } else {
                rabbitMqQueuePublisher.publish(notificationQueueDto);
            }
        } catch (Exception e) {
            log.warn("Error while send user notification : {}", e.getMessage());
        }
    }
}
