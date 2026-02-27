package com.moneyfi.notification.service.rabbitmq;

import com.moneyfi.notification.service.EmailTemplateInjector;
import com.moneyfi.notification.service.dto.NotificationQueueDto;
import com.moneyfi.notification.service.email.EmailTemplates;
import com.moneyfi.notification.util.constants.StringConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile("local-rabbitmq")
public class RabbitMqQueueListener {

    private final EmailTemplates emailTemplates;

    @RabbitListener(queues = "${rabbitmq.queue}")
    public void handleRabbitQueueEventListener(String notificationQueueDtoJson) {
        System.out.println("checking: " + notificationQueueDtoJson);
        NotificationQueueDto notificationQueueDto = StringConstants.objectMapper.readValue(notificationQueueDtoJson, NotificationQueueDto.class);
        EmailTemplateInjector.functionToRouteBasedOnRequest(notificationQueueDto, emailTemplates);
    }
}