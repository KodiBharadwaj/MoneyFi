package com.moneyfi.user.service.user.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountBlockOrDeleteRequestDto {
    @NotBlank
    private String otp;
    @NotBlank
    private String deactivationType;
    private String password;
    @Column(columnDefinition = "TEXT")
    @NotBlank
    private String description;
}
