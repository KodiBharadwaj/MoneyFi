package com.moneyfi.transaction.batch.dto;

import lombok.Builder;

@Builder
public class UserNotification {
    private String username;
    private Long scheduleId;
    private boolean isRead;
}
