package com.moneyfi.user.service.general.artemis.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.communication.queue-provider", havingValue = "artemis", matchIfMissing = true)
public class ArtemisQueueListener {

    @JmsListener(destination = "${spring.artemis.feedback-queue}")
    public void receiveMessage(String message) {
        log.info("Received feedback from notification service: {}", message);
    }
}
