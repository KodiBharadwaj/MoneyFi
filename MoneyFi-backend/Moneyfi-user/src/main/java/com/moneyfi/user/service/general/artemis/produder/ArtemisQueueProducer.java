package com.moneyfi.user.service.general.artemis.produder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.moneyfi.user.service.user.dto.internal.NotificationQueueDto;
import com.moneyfi.user.util.constants.StringConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArtemisQueueProducer {

    @Value("${spring.artemis.queue-name}")
    private String producerQueue;

    private final JmsTemplate jmsTemplate;

    public void sendMessage(NotificationQueueDto notificationQueueDto) throws JsonProcessingException {
        jmsTemplate.convertAndSend(producerQueue, StringConstants.objectMapper.writeValueAsString(notificationQueueDto));
    }
}
