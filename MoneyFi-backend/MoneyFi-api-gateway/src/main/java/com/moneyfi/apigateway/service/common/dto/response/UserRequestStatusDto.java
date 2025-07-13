package com.moneyfi.apigateway.service.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestStatusDto {
    private String email;
    private String name;
    private String requestStatus;
    private String requestType;
    private String isRequestActive;
    private String description;
}
