package com.moneyfi.user.service.admin.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

import static com.moneyfi.user.util.constants.StringConstants.DATE_TIME_PATTERN;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminScheduleRequestDto {
    private Long scheduleId;
    private String subject;
    private String description;
    private String recipients;
    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private Timestamp scheduleFrom;
    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private Timestamp scheduleTo;
}
