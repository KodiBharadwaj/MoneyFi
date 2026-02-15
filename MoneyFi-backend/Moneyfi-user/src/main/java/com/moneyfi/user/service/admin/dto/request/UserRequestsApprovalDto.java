package com.moneyfi.user.service.admin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestsApprovalDto {
    private String email;
    private String referenceNumber;
    private String requestStatus;
    private String declineReason;
    private String approveStatus;

    private int gmailSyncRequestCount;
}
