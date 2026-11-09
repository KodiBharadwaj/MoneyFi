package com.moneyfi.notification.service.event.publisher;

import com.moneyfi.constants.dto.FeedbackResponseDto;
import com.moneyfi.notification.service.artemis.publisher.ArtemisQueuePublisher;
import com.moneyfi.notification.service.rabbitmq.publisher.RabbitMqQueuePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import static com.moneyfi.constants.constants.CommonConstants.ARTEMIS;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisherService {

    @Value("${app.communication.queue-provider}")
    private String queueProvider;

    private final ArtemisQueuePublisher artemisQueuePublisher;
    private final RabbitMqQueuePublisher rabbitMqQueuePublisher;
    private final ObjectMapper objectMapper;

    @Async
    @Order(1)
    @EventListener
    public void publishMessage(FeedbackResponseDto feedbackResponseDto) {
        try {
            String feedbackResponseString = objectMapper.writeValueAsString(feedbackResponseDto);
            if (ARTEMIS.equalsIgnoreCase(queueProvider)) {
                artemisQueuePublisher.publishMessage(feedbackResponseString);
            } else {
                rabbitMqQueuePublisher.publishMessage(feedbackResponseString);
            }
        } catch (Exception e) {
            log.error("Error while send user feedback notification : {}", e.getMessage());
        }
    }

    @Async
    @Order(1)
    @EventListener
    public void publishMessage(String feedbackResponseDto) {
        try {
            String feedbackResponseString = objectMapper.writeValueAsString(feedbackResponseDto);
            if (ARTEMIS.equalsIgnoreCase(queueProvider)) {
                artemisQueuePublisher.publishMessage(feedbackResponseString);
            } else {
                rabbitMqQueuePublisher.publishMessage(feedbackResponseString);
            }
        } catch (Exception e) {
            log.error("Error while send user feedback notification : {}", e.getMessage());
        }
    }
}
