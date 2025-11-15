package com.moneyfi.apigateway.dto.interfaces;

import java.time.LocalDateTime;

public interface ContactUsProjection {
    Long getId();
    String getEmail();
    String getReferenceNumber();
    boolean getIsRequestActive();
    String getRequestReason();
    boolean getIsVerified();
    String getRequestStatus();
    LocalDateTime getStartTime();
    LocalDateTime getCompletedTime();
}
