package com.moneyfi.user.service.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.moneyfi.user.util.enums.TargetUsersForScheduleNotification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static com.moneyfi.user.util.constants.StringConstants.DATE_TIME_PATTERN;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminSchedulesResponseDto {
    private Long scheduleId;
    private String subject;
    private String description;
    private boolean isCancelled;
    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private Timestamp createdDate;
    private String recipients;
    private List<String> recipentList = new ArrayList<>();
    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private Timestamp scheduleFrom;
    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private Timestamp scheduleTo;
}
