package com.moneyfi.notification.service.artemis.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArtemisQueueProducer {

    @Value("${spring.artemis.feedback-queue}")
    private String feedbackQueue;

    private final JmsTemplate jmsTemplate;

    public void sendFeedbackMessage(String message) {
        jmsTemplate.convertAndSend(feedbackQueue, message);
    }
}
