package com.moneyfi.apigateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileChangePassword {
    private int otpCount;
    private boolean flag;
}
