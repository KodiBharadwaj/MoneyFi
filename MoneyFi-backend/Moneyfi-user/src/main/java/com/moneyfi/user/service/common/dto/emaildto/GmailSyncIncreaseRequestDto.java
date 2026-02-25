package com.moneyfi.user.service.common.dto.emaildto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GmailSyncIncreaseRequestDto {
    private int count;
    private String reason;
    private String name;
    private String email;
    private String imageBase64;
    private String fileName;
    private String contentType;
}
