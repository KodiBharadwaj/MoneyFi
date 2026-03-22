package com.moneyfi.user.service.user.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfile {
    @NotNull
    private String name;
    @NotNull
    private String username;
    private String password;
    @NotNull
    private String role;
}
