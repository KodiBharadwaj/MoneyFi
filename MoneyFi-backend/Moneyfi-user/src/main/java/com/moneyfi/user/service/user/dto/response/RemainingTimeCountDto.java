package com.moneyfi.user.service.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemainingTimeCountDto {
    private int remainingMinutes;
    private boolean result;
}
