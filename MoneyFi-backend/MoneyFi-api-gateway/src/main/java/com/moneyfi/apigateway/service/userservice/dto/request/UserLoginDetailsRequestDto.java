package com.moneyfi.apigateway.service.userservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginDetailsRequestDto {
    private String username;
    private String password;
    private String role;
}