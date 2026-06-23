package com.moneyfi.transaction.batch.writer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moneyfi.transaction.batch.dto.UserNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class UserSchedulingNotificationWriter implements ItemWriter<UserNotification> {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void write(Chunk<? extends UserNotification> chunk) throws JsonProcessingException {
        kafkaTemplate.send("user-scheduling-notification-topic", objectMapper.writeValueAsString(new ArrayList<>(chunk.getItems())));
    }
}
