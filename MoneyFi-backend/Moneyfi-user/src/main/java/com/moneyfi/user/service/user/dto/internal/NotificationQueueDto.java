package com.moneyfi.user.service.user.dto.internal;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationQueueDto {
    private String notificationQueueType;
    private String valueJson;
}
