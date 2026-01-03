package com.moneyfi.user.service.common.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

import static com.moneyfi.user.util.constants.StringConstants.DATE_TIME_PATTERN;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestStatusDto {
    private String email;
    private String name;
    private String requestStatus;
    private String requestType;
    private String isRequestActive;
    private String description;
    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private Timestamp requestedDate;
}
