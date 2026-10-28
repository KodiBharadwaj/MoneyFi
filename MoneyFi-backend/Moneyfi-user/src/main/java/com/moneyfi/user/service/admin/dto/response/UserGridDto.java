package com.moneyfi.user.service.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.moneyfi.user.dto.ReusableTotalCountDto;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.sql.Timestamp;
import java.util.Date;

import static com.moneyfi.user.util.constants.StringConstants.DATE_TIME_PATTERN;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserGridDto extends ReusableTotalCountDto {
    private int slNo;
    private String name;
    private String username;
    private String phone;
    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private Timestamp createdDateTime;
    private Date dateOfBirth;
}
