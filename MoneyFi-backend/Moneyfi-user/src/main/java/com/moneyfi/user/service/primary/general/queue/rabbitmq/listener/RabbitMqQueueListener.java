package com.moneyfi.user.service.primary.general.queue.rabbitmq.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moneyfi.constants.dto.FeedbackResponseDto;
import com.moneyfi.user.service.primary.general.inter.EmailNotificationFailedService;
import com.moneyfi.user.util.enums.QueueFailureStage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.communication.queue-provider", havingValue = "rabbitmq")
public class RabbitMqQueueListener {

    private final ObjectMapper objectMapper;
    private final EmailNotificationFailedService emailNotificationFailedService;

    @RabbitListener(queues = "${rabbitmq.response.queue}")
    public void handleRabbitQueueEventListener(String message) throws JsonProcessingException {
        log.info("Received feedback from notification service: {}", message);
        FeedbackResponseDto feedbackResponseDto = objectMapper.readValue(message, FeedbackResponseDto.class);
        emailNotificationFailedService.saveFailedEmailNotificationDetails(feedbackResponseDto.getRequestValueJson(), feedbackResponseDto.getErrorMessage(), QueueFailureStage.DESTINATION_SERVICE.name());
    }
}