package com.moneyfi.user.service.common.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GmailSyncCountIncreaseRequestDto {
    @NotNull
    private int count;
    @NotNull
    private String reason;
}
