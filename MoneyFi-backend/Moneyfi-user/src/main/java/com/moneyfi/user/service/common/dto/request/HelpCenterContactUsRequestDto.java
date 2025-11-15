package com.moneyfi.user.service.common.dto.request;

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
