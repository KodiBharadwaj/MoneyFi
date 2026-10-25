package com.moneyfi.notification.service.rabbitmq.listener;

import com.moneyfi.notification.service.EmailTemplateInjector;
import com.moneyfi.notification.service.dto.NotificationQueueDto;
import com.moneyfi.notification.service.email.EmailTemplates;
import com.moneyfi.notification.util.constants.StringConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.thirdparty.jackson.core.JsonProcessingException;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.communication.queue-provider", havingValue = "rabbitmq")
public class RabbitMqQueueListener {

    private final EmailTemplates emailTemplates;
    private final EmailTemplateInjector emailTemplateInjector;

    @RabbitListener(queues = "${rabbitmq.request.queue}")
    public void handleRabbitQueueEventListener(String notificationQueueDtoJson) throws JsonProcessingException {
        log.info("checking listener input: {}", notificationQueueDtoJson);
        NotificationQueueDto notificationQueueDto = StringConstants.objectMapper.readValue(notificationQueueDtoJson, NotificationQueueDto.class);
        emailTemplateInjector.functionToRouteBasedOnRequest(notificationQueueDto, emailTemplates);
    }
}