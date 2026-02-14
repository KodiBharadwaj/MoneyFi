package com.moneyfi.apigateway.service.maintainer.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrUpdateAdminRequestDto {
    @NonNull
    private String username;
    @NonNull
    private String password;
    private String comment;
}
