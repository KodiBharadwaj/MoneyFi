package com.moneyfi.user.service.common.dto.internal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GmailSyncCountJsonDto {
    private int count;
    private String reason;
    private Long contactUsId;
}
