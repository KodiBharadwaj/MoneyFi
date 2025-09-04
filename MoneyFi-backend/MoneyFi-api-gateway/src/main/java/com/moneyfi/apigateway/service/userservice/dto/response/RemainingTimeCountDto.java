package com.moneyfi.apigateway.service.userservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemainingTimeCountDto {
    private int remainingMinutes;
    private int otpCount;
    private boolean result;
    private String comment;
}
