package com.moneyfi.notification.service.rabbitmq;

import com.moneyfi.notification.service.EmailTemplateInjector;
import com.moneyfi.notification.service.dto.NotificationQueueDto;
import com.moneyfi.notification.service.email.EmailTemplates;
import com.moneyfi.notification.util.constants.StringConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static com.moneyfi.notification.util.constants.StringConstants.LOCAL_PROFILE_RABBIT_MQ;
import static com.moneyfi.notification.util.constants.StringConstants.PROD_PROFILE_RABBIT_MQ;

@Component
@RequiredArgsConstructor
@Profile({LOCAL_PROFILE_RABBIT_MQ, PROD_PROFILE_RABBIT_MQ})
@Slf4j
public class RabbitMqQueueListener {

    private final EmailTemplates emailTemplates;

    @RabbitListener(queues = "${rabbitmq.queue}")
    public void handleRabbitQueueEventListener(String notificationQueueDtoJson) {
        log.info("checking listener input: {}", notificationQueueDtoJson);
        NotificationQueueDto notificationQueueDto = StringConstants.objectMapper.readValue(notificationQueueDtoJson, NotificationQueueDto.class);
        EmailTemplateInjector.functionToRouteBasedOnRequest(notificationQueueDto, emailTemplates);
    }
}