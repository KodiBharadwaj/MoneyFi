package com.moneyfi.user.service.common.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDefectRequestDto {
    private String name;
    private String email;
    private String message;
    private MultipartFile file;
}
