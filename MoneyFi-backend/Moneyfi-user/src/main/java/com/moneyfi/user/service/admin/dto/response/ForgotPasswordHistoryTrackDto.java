package com.moneyfi.user.service.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordHistoryTrackDto {
    private String reasonForChange;
    private LocalDateTime changedAt;
}