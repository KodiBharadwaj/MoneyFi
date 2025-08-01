package com.moneyfi.apigateway.service.userservice.dto.request;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Type;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountBlockRequestDto {
    private String otp;

    @Column(columnDefinition = "TEXT")
    private String description;
}
