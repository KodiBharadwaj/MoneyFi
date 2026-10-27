package com.moneyfi.user.service.general.inter;

import lombok.Data;

@Data
public class EmailNotificationFailedDetailsDto {
    private String mdcRequestId;
    private String referenceNumber;
    private String message;
    private String username;
    private String userId;
    private String name;
}
