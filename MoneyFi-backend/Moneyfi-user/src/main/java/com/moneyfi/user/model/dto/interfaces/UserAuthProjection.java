package com.moneyfi.user.model.dto.interfaces;

import java.time.LocalDateTime;

public interface UserAuthProjection {
    Long getId();
    String getUsername();
    String getPassword();
    String getVerificationCode();
    LocalDateTime getVerificationCodeExpiration();
    Integer getOtpCount();
    Boolean getIsBlocked();
    Boolean getIsDeleted();
    Integer getLoginCodeValue();
    Integer getRoleId();
}