package com.moneyfi.apigateway.service.common.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountRetrieveRequestDto {
    private String username;
    private String name;
    private String description;
    private String referenceNumber;
    private String requestReason;
}
