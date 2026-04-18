package com.moneyfi.user.service.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReasonUpdateRequestDto {
    @NotNull
    private Integer reasonId;
    @NotBlank
    private String reason;
}
