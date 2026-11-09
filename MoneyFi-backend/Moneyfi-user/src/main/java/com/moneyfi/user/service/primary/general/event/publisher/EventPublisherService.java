package com.moneyfi.user.service.primary.general.event.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.moneyfi.constants.dto.NotificationQueueDto;
import com.moneyfi.user.service.primary.general.inter.EmailNotificationFailedService;
import com.moneyfi.user.service.primary.general.queue.PublishQueueService;
import com.moneyfi.user.util.enums.QueueFailureStage;
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
    private final EmailNotificationFailedService emailNotificationFailedService;

    @Async
    @Order(1)
    @EventListener
    public void sendUserNotification(NotificationQueueDto notificationQueueDto) throws JsonProcessingException {
        try {
            publishQueueService.publishMessage(notificationQueueDto);
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            log.error("Error while sending user notification : {}", errorMessage);
            emailNotificationFailedService.saveFailedEmailNotificationDetails(notificationQueueDto, errorMessage, QueueFailureStage.INTERMEDIATE_BROKER.name());
        }
    }
}
