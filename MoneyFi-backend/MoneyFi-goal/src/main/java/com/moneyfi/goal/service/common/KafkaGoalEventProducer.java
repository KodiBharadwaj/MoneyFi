package com.moneyfi.goal.service.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import static com.moneyfi.goal.utils.StringConstants.TOPIC;

@Service
public class KafkaGoalEventProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendExpenseIds(String expenseIds) {
        kafkaTemplate.send(TOPIC, expenseIds);
    }
}
