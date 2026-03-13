package com.moneyfi.user.service.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

import static com.moneyfi.user.util.constants.StringConstants.DATE_TIME_PATTERN;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestsGridDto {
    private Long requestId;
    private String username;
    private String name;
    private String requestType;
    private String referenceNumber;
    private String description;

    @JsonIgnore
    @JsonFormat(pattern = DATE_TIME_PATTERN, timezone = "Asia/Kolkata")
    private Timestamp requestedOn;
    private Integer daysLeft;
}
