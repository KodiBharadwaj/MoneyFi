package com.moneyfi.notification.service.dto.emaildto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GmailSyncIncreaseRequestDto {
    private int count;
    private String reason;
    private String name;
    private String email;
    private MultipartFile image;
}
