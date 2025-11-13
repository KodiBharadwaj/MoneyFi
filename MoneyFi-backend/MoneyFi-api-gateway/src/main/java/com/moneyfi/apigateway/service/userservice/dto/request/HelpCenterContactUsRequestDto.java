package com.moneyfi.apigateway.service.userservice.dto.request;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HelpCenterContactUsRequestDto {
    private String email;
    private String phoneNumber;
    private String name;
    private String description;
}
