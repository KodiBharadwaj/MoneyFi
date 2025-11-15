package com.moneyfi.user.service.admin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReasonDetailsRequestDto {
    private Integer reasonCode;
    private String reason;
}
