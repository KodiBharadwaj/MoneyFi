package com.moneyfi.user.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtpTempModel {
    private Long id;
    private String email;
    private String otp;
    private LocalDateTime expirationTime;
    private String otpType;
}
