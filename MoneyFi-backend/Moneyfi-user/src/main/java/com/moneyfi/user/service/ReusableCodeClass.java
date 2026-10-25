package com.moneyfi.user.service;

import com.moneyfi.user.service.user.dto.internal.NotificationQueueDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReusableCodeClass {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void sendNotificationToUser(String notificationQueueType, String description) {
        log.info("Sending Notification to user of type: {}", notificationQueueType);
        applicationEventPublisher.publishEvent(
                NotificationQueueDto.builder()
                        .notificationQueueType(notificationQueueType)
                        .valueJson(description)
                        .build()
        );
    }
}
