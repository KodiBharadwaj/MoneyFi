package com.moneyfi.apigateway.service.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Timestamp;

import static com.moneyfi.apigateway.util.constants.StringUtils.DATE_TIME_PATTERN;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileAndRequestDetailsDto {
    private String name;
    private String username;
    private String phoneNumber;
    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private Timestamp createdTime;
    private String gender;
    private String maritalStatus;
    private Date dateOfBirth;
    private String address;
    private int activeRequests;
    private int completedRequests;
}
