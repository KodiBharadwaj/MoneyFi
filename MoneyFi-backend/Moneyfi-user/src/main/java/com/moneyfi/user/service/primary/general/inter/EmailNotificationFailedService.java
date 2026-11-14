package com.moneyfi.user.service.primary.general.inter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.moneyfi.constants.dto.NotificationQueueDto;
import com.moneyfi.constants.dto.PaginatedRequestDto;
import com.moneyfi.user.service.primary.admin.dto.response.FailedQueueDetailsResponseDto;

import java.time.OffsetDateTime;
import java.util.List;

public interface EmailNotificationFailedService {
    void saveFailedEmailNotificationDetails(NotificationQueueDto notificationQueueDto, String errorMessage, String failureStage) throws JsonProcessingException;

    void saveFailedEmailNotificationDetails(String username, String errorMessage, String failureStage, String messageType) throws JsonProcessingException;

    List<FailedQueueDetailsResponseDto> getFailedQueueEmailNotificationDetails(OffsetDateTime startDate, OffsetDateTime endDate, PaginatedRequestDto requestDto);
}
