package com.moneyfi.apigateway.service.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeHistoryTrackDto {
    private String reasonForChange;
    private LocalDateTime changedAt;
}