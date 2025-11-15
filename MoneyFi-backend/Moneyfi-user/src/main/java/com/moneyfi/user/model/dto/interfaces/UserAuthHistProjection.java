package com.moneyfi.user.model.dto.interfaces;

import java.time.LocalDateTime;

public interface UserAuthHistProjection {
    Long getId();
    Long getUserId();
    LocalDateTime getUpdatedTime();
    String getComment();
    Long getUpdatedBy();
    int getReasonTypeId();
}
