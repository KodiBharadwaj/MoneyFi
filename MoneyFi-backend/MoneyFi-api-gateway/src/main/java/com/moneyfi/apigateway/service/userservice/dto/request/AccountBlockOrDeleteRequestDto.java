package com.moneyfi.apigateway.service.userservice.dto.request;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountBlockOrDeleteRequestDto {
    private String otp;
    private String deactivationType;
    private String password;
    @Column(columnDefinition = "TEXT")
    private String description;
}
