package com.moneyfi.user.service.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GmailSyncCountIncreaseRequestDto {
    @NotNull
    private int count;
    @NotBlank
    private String reason;
}
