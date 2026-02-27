package com.moneyfi.notification.service.artemis;

import com.moneyfi.notification.service.EmailTemplateInjector;
import com.moneyfi.notification.service.dto.NotificationQueueDto;
import com.moneyfi.notification.service.email.EmailTemplates;
import com.moneyfi.notification.util.constants.StringConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@Profile("local-artemis")
@RequiredArgsConstructor
public class ArtemisQueueConsumer {

    private final EmailTemplates emailTemplates;

    @JmsListener(destination = "artemis.queue.name")
    public void receiveMessage(String message) {
        System.out.println("Received from Artemis: " + message);
        NotificationQueueDto notificationQueueDto = StringConstants.objectMapper.readValue(message, NotificationQueueDto.class);
        EmailTemplateInjector.functionToRouteBasedOnRequest(notificationQueueDto, emailTemplates);
    }
}
