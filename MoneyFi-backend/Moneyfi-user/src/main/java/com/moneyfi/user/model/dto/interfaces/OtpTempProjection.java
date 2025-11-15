package com.moneyfi.user.model.dto.interfaces;

import java.time.LocalDateTime;

public interface OtpTempProjection {
    Long getId();
    String getEmail();
    String getOtp();
    LocalDateTime getExpirationTime();
    String getOtpType();
}
