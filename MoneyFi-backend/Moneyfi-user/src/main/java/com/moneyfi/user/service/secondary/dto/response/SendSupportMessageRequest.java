package com.moneyfi.user.service.secondary.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendSupportMessageRequest(
        @NotBlank
        @Size(max = 5000)
        String content
) {}
