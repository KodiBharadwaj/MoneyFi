package com.moneyfi.apigateway.dto.interfaces;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface ProfileDetailsProjection {
    Long getId();
    Long getUserId();
    String getName();
    LocalDateTime getCreatedDate();
    String getPhone();
    String getGender();
    LocalDate getDateOfBirth();
    String getMaritalStatus();
    String getAddress();
    double getIncomeRange();
}
