package com.moneyfi.user.service.general.queue.artemis.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.moneyfi.user.service.general.inter.EmailNotificationFailedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.communication.queue-provider", havingValue = "artemis", matchIfMissing = true)
public class ArtemisQueueListener {

    private final EmailNotificationFailedService emailNotificationFailedService;

    @JmsListener(destination = "${spring.artemis.feedback-queue}")
    public void receiveMessage(String message) throws JsonProcessingException {
        log.info("Received feedback from notification service: {}", message);
        emailNotificationFailedService.saveFailedEmailNotificationDetails(message);
    }
}
