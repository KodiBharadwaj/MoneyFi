package com.moneyfi.notification.service.event.publisher;

import com.moneyfi.notification.service.artemis.publisher.ArtemisQueuePublisher;
import com.moneyfi.notification.service.rabbitmq.publisher.RabbitMqQueuePublisher;
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

    private final ArtemisQueuePublisher artemisQueuePublisher;
    private final RabbitMqQueuePublisher rabbitMqQueuePublisher;

    @Async
    @Order(1)
    @EventListener
    public void publishMessage(String message) {
        try {
            if (ARTEMIS.equalsIgnoreCase(queueProvider)) {
                artemisQueuePublisher.publishMessage(message);
            } else {
                rabbitMqQueuePublisher.publishMessage(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.warn("Error while send user notification : {}", e.getMessage());
        }
    }
}
