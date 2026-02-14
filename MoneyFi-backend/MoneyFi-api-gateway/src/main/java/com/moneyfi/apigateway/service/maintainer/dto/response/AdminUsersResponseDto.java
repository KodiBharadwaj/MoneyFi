package com.moneyfi.apigateway.service.maintainer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUsersResponseDto {
    private Long id;
    private String username;
}