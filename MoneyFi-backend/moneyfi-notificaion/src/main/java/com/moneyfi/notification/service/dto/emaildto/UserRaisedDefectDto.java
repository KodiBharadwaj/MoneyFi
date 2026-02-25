package com.moneyfi.notification.service.dto.emaildto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRaisedDefectDto {
    private String message;
    private String name;
    private String email;
    private String imageBase64;
    private String fileName;
    private String contentType;
}
