package com.moneyfi.user.service.general.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.moneyfi.user.service.user.dto.internal.NotificationQueueDto;

public interface PublishQueueService {
    void publishMessage(NotificationQueueDto notificationQueueDto) throws JsonProcessingException;
}
