package com.moneyfi.user.service.user.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginDetailsRequestDto {
    @NotNull
    private String username;
    @NotNull
    private String password;
    @NotNull
    private String role;
}