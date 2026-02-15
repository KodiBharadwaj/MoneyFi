package com.moneyfi.user.service.common.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

import static com.moneyfi.user.util.constants.StringConstants.DATE_TIME_PATTERN;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserNotificationResponseDto {
    private Long notificationId;
    private String subject;
    private String description;
    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private Timestamp scheduleFrom;
    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private Timestamp scheduleTo;
    private boolean isRead;
    @JsonIgnore
    private Long scheduleId;
    @JsonIgnore
    private String notificationType;
    @JsonIgnore
    private String role;
    @JsonIgnore
    private Long parentKey;
}
