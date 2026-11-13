package com.moneyfi.user.service.primary.maintainer.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@Builder
public class SupportUsersResponseDto {
    private Long id;
    private String username;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isRegistered;
}