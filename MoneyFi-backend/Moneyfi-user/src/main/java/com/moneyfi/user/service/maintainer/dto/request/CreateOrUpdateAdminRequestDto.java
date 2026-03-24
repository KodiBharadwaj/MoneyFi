package com.moneyfi.user.service.maintainer.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrUpdateAdminRequestDto {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    private String comment;
}
