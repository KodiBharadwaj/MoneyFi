package com.moneyfi.user.service.general.event.publisher;

import com.moneyfi.user.service.general.artemis.ArtemisQueueProducer;
import com.moneyfi.user.service.user.dto.internal.NotificationQueueDto;
import com.moneyfi.user.service.general.rabbitmq.RabbitMqQueuePublisher;
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
