package com.moneyfi.apigateway.service.general;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.moneyfi.apigateway.service.general.dto.NotificationQueueDto;
import com.moneyfi.apigateway.util.constants.StringConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArtemisQueueProducer {

    private final JmsTemplate jmsTemplate;

    public void sendMessage(NotificationQueueDto notificationQueueDto) throws JsonProcessingException {
        jmsTemplate.convertAndSend("artemis.queue.name", StringConstants.objectMapper.writeValueAsString(notificationQueueDto));
    }
}
