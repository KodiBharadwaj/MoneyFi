package com.moneyfi.apigateway.service.admin.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDefectResponseDto {
    private String name;
    private String username;
    private String referenceNumber;
    private String description;
    private String imageId;
    @JsonIgnore
    private boolean isActive;
}
