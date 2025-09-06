package com.moneyfi.apigateway.service.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDefectResponseDto {
    private Long defectId;
    private String name;
    private String username;
    private String referenceNumber;
    private String description;
    private String imageId;
    private String defectStatus;
}
