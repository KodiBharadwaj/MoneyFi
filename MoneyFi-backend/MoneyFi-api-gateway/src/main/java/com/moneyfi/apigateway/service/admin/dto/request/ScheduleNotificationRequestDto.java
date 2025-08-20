package com.moneyfi.apigateway.service.admin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleNotificationRequestDto {
    private String subject;
    private String description;
    private LocalDateTime scheduleFrom;
    private LocalDateTime scheduleTo;
    private String recipients;
}
