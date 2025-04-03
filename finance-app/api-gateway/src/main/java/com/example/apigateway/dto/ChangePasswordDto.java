package com.example.apigateway.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordDto {
    private Integer userId;
    private String currentPassword;
    private String newPassword;
}
