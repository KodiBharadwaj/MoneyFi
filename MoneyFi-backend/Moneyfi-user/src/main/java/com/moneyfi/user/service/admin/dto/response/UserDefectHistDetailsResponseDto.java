package com.moneyfi.user.service.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDefectHistDetailsResponseDto {
    private String status;
    private String description;
    private LocalDateTime actionTime;
}