package com.moneyfi.user.service.general.artemis.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArtemisQueueConsumer {

    @JmsListener(destination = "artemis.queue.producer-user.consumer-notification.feedback")
    public void receiveMessage(String message) {
        log.info("Received feedback from notification service: {}", message);
    }
}
