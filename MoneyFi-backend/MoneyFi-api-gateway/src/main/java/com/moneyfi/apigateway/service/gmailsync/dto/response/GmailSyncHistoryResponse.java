package com.moneyfi.apigateway.service.gmailsync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GmailSyncHistoryResponse {
    private LocalDateTime syncTime;
    private Integer syncCount;
}
