package com.moneyfi.apigateway.service.common.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFeedbackRequestDto {
    private String email;
    private String message;
    private String name;
}
