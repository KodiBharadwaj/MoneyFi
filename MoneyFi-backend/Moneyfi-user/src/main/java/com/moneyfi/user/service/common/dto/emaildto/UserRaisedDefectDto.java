package com.moneyfi.user.service.common.dto.emaildto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

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
