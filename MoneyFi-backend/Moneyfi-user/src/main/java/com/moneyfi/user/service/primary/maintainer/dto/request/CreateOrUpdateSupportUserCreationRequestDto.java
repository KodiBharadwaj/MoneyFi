package com.moneyfi.user.service.primary.maintainer.dto.request;

import com.moneyfi.constants.enums.UserRoles;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrUpdateSupportUserCreationRequestDto {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    private String comment;
    @NotNull
    private UserRoles role;
}
