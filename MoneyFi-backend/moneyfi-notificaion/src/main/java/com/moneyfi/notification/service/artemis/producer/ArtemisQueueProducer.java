package com.moneyfi.notification.service.artemis.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArtemisQueueProducer {

    private final JmsTemplate jmsTemplate;

    public void sendFeedbackMessage(String message) {
        jmsTemplate.convertAndSend("artemis.queue.producer-user.consumer-notification.feedback", message);
    }
}
