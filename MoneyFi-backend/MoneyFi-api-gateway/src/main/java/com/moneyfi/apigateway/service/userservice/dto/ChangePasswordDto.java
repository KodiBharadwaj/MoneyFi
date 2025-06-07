package com.moneyfi.apigateway.service.userservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordDto {
    private Long userId;
    private String currentPassword;
    private String newPassword;
}
