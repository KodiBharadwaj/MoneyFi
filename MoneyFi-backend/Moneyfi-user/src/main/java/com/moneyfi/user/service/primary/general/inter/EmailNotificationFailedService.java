package com.moneyfi.user.service.primary.general.inter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.moneyfi.constants.dto.NotificationQueueDto;

public interface EmailNotificationFailedService {
    void saveFailedEmailNotificationDetails(NotificationQueueDto notificationQueueDto, String errorMessage, String failureStage) throws JsonProcessingException;

    void saveFailedEmailNotificationDetails(Long userId, String username, String errorMessage, String failureStage, String messageType) throws JsonProcessingException;
}
