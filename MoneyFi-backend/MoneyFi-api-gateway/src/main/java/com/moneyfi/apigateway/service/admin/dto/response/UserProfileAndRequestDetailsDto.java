package com.moneyfi.apigateway.service.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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
    @JsonIgnore
    private Long userId;
//    private ResponseEntity<ByteArrayResource> profileImage;

    private AdminUserRequestsCountDto userRequestCount;
    private List<AdminUserNameChangeDetailsDto> nameChangeRequests = new ArrayList<>();
    private List<AdminUserUnblockRequestDetailsDto> unblockAccountRequests = new ArrayList<>();
}
