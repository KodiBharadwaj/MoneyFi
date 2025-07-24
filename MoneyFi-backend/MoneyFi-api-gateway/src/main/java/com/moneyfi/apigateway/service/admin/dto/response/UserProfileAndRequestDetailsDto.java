package com.moneyfi.apigateway.service.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;

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
    private String imageId;
    private int activeRequestsCount;
    private int completedRequestsCount;
    private int issuesRaisedCount;
    private int feedbackCount;

    @JsonIgnore
    private Long userId;
    private ResponseEntity<ByteArrayResource> imageFromS3;
}
