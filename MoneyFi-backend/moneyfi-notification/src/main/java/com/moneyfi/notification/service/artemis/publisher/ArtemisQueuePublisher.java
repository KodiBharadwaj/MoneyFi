package com.moneyfi.notification.service.artemis.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArtemisQueuePublisher {

    @Value("${spring.artemis.feedback-queue}")
    private String feedbackQueue;

    private final JmsTemplate jmsTemplate;

    public void publishMessage(String message) {
        log.info("Feedback notification sent");
        jmsTemplate.convertAndSend(feedbackQueue, message);
    }
}
