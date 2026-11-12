package com.moneyfi.user.service.primary.general.queue.artemis.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moneyfi.constants.dto.FeedbackResponseDto;
import com.moneyfi.user.service.primary.general.inter.EmailNotificationFailedService;
import com.moneyfi.user.util.enums.QueueFailureStage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.communication.queue-provider", havingValue = "artemis", matchIfMissing = true)
public class ArtemisQueueListener {

    private final EmailNotificationFailedService emailNotificationFailedService;
    private final ObjectMapper objectMapper;

    @JmsListener(destination = "${spring.artemis.feedback-queue}")
    public void receiveMessage(String message) throws JsonProcessingException {
        log.info("Received feedback from notification service: {}", message);
        FeedbackResponseDto feedbackResponseDto = objectMapper.readValue(message, FeedbackResponseDto.class);
        emailNotificationFailedService.saveFailedEmailNotificationDetails(feedbackResponseDto.getRequestValueJson(), feedbackResponseDto.getErrorMessage(), QueueFailureStage.DESTINATION_SERVICE.name());
    }
}
