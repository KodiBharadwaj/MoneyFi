package com.moneyfi.user.service.general.queue.artemis.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.moneyfi.user.service.general.queue.PublishQueueService;
import com.moneyfi.user.service.user.dto.internal.NotificationQueueDto;
import com.moneyfi.user.util.constants.StringConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.communication.queue-provider", havingValue = "artemis", matchIfMissing = true)
public class ArtemisQueueServicePublisher implements PublishQueueService {

    @Value("${spring.artemis.queue-name}")
    private String producerQueue;

    private final JmsTemplate jmsTemplate;

    public void publishMessage(NotificationQueueDto notificationQueueDto) throws JsonProcessingException {
        jmsTemplate.convertAndSend(producerQueue, StringConstants.objectMapper.writeValueAsString(notificationQueueDto));
    }
}
