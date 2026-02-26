package com.moneyfi.user.service.common.dto.emaildto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminBlockUserDto {
    private String email;
    private String reason;
    private String name;
    private byte[] file;
}
