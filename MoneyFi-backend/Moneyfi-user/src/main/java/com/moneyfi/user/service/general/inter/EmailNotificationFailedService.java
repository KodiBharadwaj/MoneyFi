package com.moneyfi.user.service.general.inter;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface EmailNotificationFailedService {
    void saveFailedEmailNotificationDetails(String message) throws JsonProcessingException;
}
