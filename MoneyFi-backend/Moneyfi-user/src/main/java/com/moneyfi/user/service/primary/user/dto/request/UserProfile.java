package com.moneyfi.user.service.primary.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfile {
    @NotBlank
    private String name;
    @NotBlank
    private String username;
    private String password;
    @NotBlank
    private String role;
}
