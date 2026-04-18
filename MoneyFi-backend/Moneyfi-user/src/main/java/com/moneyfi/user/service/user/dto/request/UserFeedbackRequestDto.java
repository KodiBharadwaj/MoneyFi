package com.moneyfi.user.service.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFeedbackRequestDto {
    @NotBlank
    private String email;
    @NotBlank
    private String message;
    @NotBlank
    private String name;
}
