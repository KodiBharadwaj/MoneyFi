package com.moneyfi.user.service.user.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationQueueDto {
    private String notificationQueueType;
    private String valueJson;
}
