package com.moneyfi.user.service.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestsGridDto {
    private Long requestId;
    private String username;
    private String name;
    private String requestType;
    private String referenceNumber;
    private String description;
}
