package com.moneyfi.apigateway.service.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class
ForgotUsernameDto {
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String name;
    private String gender;
    private String maritalStatus;
    private String pinCode;
}
