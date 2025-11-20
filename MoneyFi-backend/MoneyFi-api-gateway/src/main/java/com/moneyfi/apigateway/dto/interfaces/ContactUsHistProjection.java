package com.moneyfi.apigateway.dto.interfaces;

import java.time.LocalDateTime;

public interface ContactUsHistProjection {
    Long getId();
    Long getContactUsId();
    String getName();
    String getMessage();
    LocalDateTime getUpdatedTime();
    String getRequestReason();
    String getRequestStatus();
}
