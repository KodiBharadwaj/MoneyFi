package com.moneyfi.user.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthModel {
    private Long id;
    private String username;
    private String password;
    private String verificationCode;
    private LocalDateTime verificationCodeExpiration;
    private int otpCount;
    private boolean isBlocked;
    private boolean isDeleted;
    private Integer loginCodeValue;
    private int roleId;
}
