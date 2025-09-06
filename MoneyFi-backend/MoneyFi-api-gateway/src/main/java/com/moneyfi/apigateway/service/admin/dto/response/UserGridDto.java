package com.moneyfi.apigateway.service.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Date;

import static com.moneyfi.apigateway.util.constants.StringUtils.DATE_TIME_PATTERN;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserGridDto {
    private int slNo;
    private String name;
    private String username;
    private String phone;
    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private Timestamp createdDateTime;
    private Date dateOfBirth;
}
