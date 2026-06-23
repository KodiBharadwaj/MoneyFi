package com.moneyfi.user.service.general.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moneyfi.user.model.general.UserNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class KafkaEventConsumer {

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "user-scheduling-notification-topic", groupId = "schedule-notification-group")
    public void kafkaConsumerToDeleteExpense(String kafkaPayloadList) throws JsonProcessingException {
        List<UserNotification> userNotificationList = objectMapper.readValue(kafkaPayloadList, new TypeReference<List<UserNotification>>() {});

        System.out.println(userNotificationList);
        System.out.println("--------------------------------------------------------");
    }
}
