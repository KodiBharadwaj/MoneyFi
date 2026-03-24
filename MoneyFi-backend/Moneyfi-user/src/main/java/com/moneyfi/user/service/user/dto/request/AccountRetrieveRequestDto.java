package com.moneyfi.user.service.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountRetrieveRequestDto {
    @NotBlank
    private String username;
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @NotBlank
    private String referenceNumber;
    @NotBlank
    private String requestReason;
}
