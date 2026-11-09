package com.moneyfi.user.service.primary.general.inter;

import com.moneyfi.constants.enums.NotificationQueueEnum;
import com.moneyfi.user.util.enums.QueueFailureStage;
import lombok.Data;

@Data
public class EmailNotificationFailedDetailsDto {
    private String mdcRequestId;
    private String referenceNumber;
    private String message;
    private String username;
    private String name;

    private QueueFailureStage failureStage;
    private NotificationQueueEnum messageType;
}
