package com.moneyfi.user.service.general.event.publisher;

import com.moneyfi.user.service.general.artemis.publisher.ArtemisQueuePublisher;
import com.moneyfi.user.service.user.dto.internal.NotificationQueueDto;
import com.moneyfi.user.service.general.rabbitmq.publisher.RabbitMqQueuePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import static com.moneyfi.constants.constants.CommonConstants.ARTEMIS;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisherService {

    @Value("${app.communication.queue-provider}")
    private String queueProvider;

    private final RabbitMqQueuePublisher rabbitMqQueuePublisher;
    private final ArtemisQueuePublisher artemisQueuePublisher;

    @Async
    @Order(1)
    @EventListener
    public void sendUserNotification(NotificationQueueDto notificationQueueDto) {
        try {
            if (ARTEMIS.equalsIgnoreCase(queueProvider)) {
                artemisQueuePublisher.publishMessage(notificationQueueDto);
            } else {
                rabbitMqQueuePublisher.publishMessage(notificationQueueDto);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error while send user notification : {}", e.getMessage());
        }
    }
}
