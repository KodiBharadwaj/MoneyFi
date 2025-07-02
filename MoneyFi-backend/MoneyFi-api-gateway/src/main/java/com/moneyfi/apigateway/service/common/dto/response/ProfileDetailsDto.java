package com.moneyfi.apigateway.service.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDetailsDto {
    private String name;
    private String email;
    private String phone;
    private String gender;
    private String maritalStatus;
    private Date dateOfBirth;
    private String address;
    private double incomeRange;
    private String profileImage;
    private Date createdDate;
}
