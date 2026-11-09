package com.moneyfi.user.service.primary.general.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.moneyfi.constants.dto.NotificationQueueDto;

public interface PublishQueueService {
    void publishMessage(NotificationQueueDto notificationQueueDto) throws JsonProcessingException;
}
