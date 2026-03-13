package com.moneyfi.transaction.service.transaction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GmailSyncErrorResponse {
    private Long gmailProcessedId;
    private List<Map<String, List<String>>> errorColumns;
}
