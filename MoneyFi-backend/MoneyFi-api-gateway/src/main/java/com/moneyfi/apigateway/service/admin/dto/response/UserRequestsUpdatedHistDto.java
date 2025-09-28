package com.moneyfi.apigateway.service.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

import static com.moneyfi.apigateway.util.constants.StringUtils.DATE_TIME_PATTERN;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestsUpdatedHistDto {
    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private Timestamp requestUpdateTime;
}
