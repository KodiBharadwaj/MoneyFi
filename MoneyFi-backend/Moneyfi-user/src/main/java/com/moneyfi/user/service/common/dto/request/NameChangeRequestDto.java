package com.moneyfi.user.service.common.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NameChangeRequestDto {
    private String email;
    private String oldName;
    private String newName;
    private String referenceNumber;
    private String description;
}
