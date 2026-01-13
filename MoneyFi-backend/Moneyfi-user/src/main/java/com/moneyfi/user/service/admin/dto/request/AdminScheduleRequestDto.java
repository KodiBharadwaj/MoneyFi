package com.moneyfi.user.service.admin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminScheduleRequestDto {
    private Long scheduleId;
    private String subject;
    private String description;
    private String recipients;
    private LocalDateTime scheduleFrom;
    private LocalDateTime scheduleTo;
}
