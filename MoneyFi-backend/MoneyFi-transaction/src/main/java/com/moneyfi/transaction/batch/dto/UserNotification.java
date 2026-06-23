package com.moneyfi.transaction.batch.dto;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Builder
@Getter
public class UserNotification implements Serializable {
    private String username;
    private Long scheduleId;
    private boolean isRead;
}
