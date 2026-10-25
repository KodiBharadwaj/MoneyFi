package com.moneyfi.notification.service.artemis.listener;

import com.moneyfi.notification.service.EmailTemplateInjector;
import com.moneyfi.notification.service.dto.NotificationQueueDto;
import com.moneyfi.notification.service.email.EmailTemplates;
import com.moneyfi.notification.util.constants.StringConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.thirdparty.jackson.core.JsonProcessingException;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.communication.queue-provider", havingValue = "artemis", matchIfMissing = true)
public class ArtemisQueueListener {

    private final EmailTemplates emailTemplates;
    private final EmailTemplateInjector emailTemplateInjector;

    @JmsListener(destination = "${spring.artemis.queue-name}")
    public void receiveMessage(String message) throws JsonProcessingException {
        log.info("Received from Artemis: {}", message);
        NotificationQueueDto notificationQueueDto = StringConstants.objectMapper.readValue(message, NotificationQueueDto.class);

        if (StringUtils.isBlank(notificationQueueDto.getNotificationQueueType())) log.error("Invalid Notification Type");
        emailTemplateInjector.functionToRouteBasedOnRequest(notificationQueueDto, emailTemplates);
    }
}
