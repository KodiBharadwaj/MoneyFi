package com.moneyfi.apigateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileModel {
    private Long id;
    private Long userId;
    private String name;
    private LocalDateTime createdDate;
    private String phone;
    private String gender;
    private LocalDate dateOfBirth;
    private String maritalStatus;
    private String address;
    private double incomeRange;
}