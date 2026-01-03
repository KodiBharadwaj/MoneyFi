package com.moneyfi.user.service.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static com.moneyfi.user.util.constants.StringConstants.DATE_TIME_PATTERN;

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
    @JsonIgnore
    private Integer loginCodeValue;
    private String accountCreationSource;
//    private ResponseEntity<ByteArrayResource> profileImage;

    private AdminUserRequestsCountDto userRequestCount;
    private List<AdminUserNameChangeDetailsDto> nameChangeRequests = new ArrayList<>();
    private List<AdminUserUnblockRequestDetailsDto> unblockAccountRequests = new ArrayList<>();
    private List<AdminUserAccRetrievalRequestDetailsDto> accountRetrievalRequests = new ArrayList<>();

    private List<PasswordChangeHistoryTrackDto> passwordChangeHistoryTrackDtoList = new ArrayList<>();
    private List<ForgotPasswordHistoryTrackDto> forgotPasswordHistoryTrackDtoList = new ArrayList<>();

    private List<UserDefectTrackingForAdminDto> userDefectTrackingForAdminDtoList = new ArrayList<>();
}
