package com.moneyfi.user.service.general.event.publisher;

import com.moneyfi.user.service.general.queue.PublishQueueService;
import com.moneyfi.user.service.user.dto.internal.NotificationQueueDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisherService {

    @Value("${app.communication.queue-provider}")
    private String queueProvider;

    private final PublishQueueService publishQueueService;

    @Async
    @Order(1)
    @EventListener
    public void sendUserNotification(NotificationQueueDto notificationQueueDto) {
        try {
            publishQueueService.publishMessage(notificationQueueDto);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error while send user notification : {}", e.getMessage());
        }
    }
}
