package com.moneyfi.notification.service;

import com.moneyfi.notification.service.dto.NotificationQueueDto;
import com.moneyfi.notification.service.email.EmailTemplates;
import com.moneyfi.notification.util.constants.StringConstants;
import com.moneyfi.notification.util.enums.NotificationQueueEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RabbitMqQueueListener {

    private final EmailTemplates emailTemplates;

    @RabbitListener(queues = "${rabbitmq.queue}")
    public void handleRabbitQueueEventListener(String notificationQueueDtoJson) {
        System.out.println("checking: " + notificationQueueDtoJson);
        NotificationQueueDto notificationQueueDto = StringConstants.objectMapper.readValue(notificationQueueDtoJson, NotificationQueueDto.class);
        functionToRouteBasedOnRequest(notificationQueueDto);
    }

    private void functionToRouteBasedOnRequest(NotificationQueueDto notificationQueueDto) {
        if(notificationQueueDto.getNotificationQueueType().equalsIgnoreCase(NotificationQueueEnum.USER_FEEDBACK_MAIL.name())) {
            functionCallForSendingFeedBackMailToAdmin(notificationQueueDto);
        } else if(notificationQueueDto.getNotificationQueueType().equalsIgnoreCase(NotificationQueueEnum.USER_DEFECT_STATUS_MAIL.name())) {
            functionCallForSendingReportStatusToUser(notificationQueueDto);
        }
    }

    private void functionCallForSendingReportStatusToUser(NotificationQueueDto notificationQueueDto) {
        String[] parts = notificationQueueDto.getValueJson().split(java.util.regex.Pattern.quote("<|>"));
        String name = parts[0];
        String referenceNumber = parts[1];
        String description = parts[2];
        String email = parts[3];
        emailTemplates.sendUserReportStatusMailToUser(name, referenceNumber, description, email);
    }

    private void functionCallForSendingFeedBackMailToAdmin(NotificationQueueDto notificationQueueDto) {
        String[] parts = notificationQueueDto.getValueJson().split(java.util.regex.Pattern.quote("<|>"));
        String rating = parts[0];
        String message = parts[1];
        emailTemplates.sendUserFeedbackEmailToAdmin(rating, message);
    }
}