package com.moneyfi.user.service.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class GmailSyncHistoryTrackDto {
    private LocalDateTime startDate;
    private String totalTimeTaken;
    private String referenceNumber;
    private String status;
    private String requestedReasonAndCount;
    private String adminRemarks;
    private Long requestId;
    private String requestDoneBy;
}
