package com.moneyfi.user.service.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
public class UserRequestsApprovalDto {
    @NotBlank
    private String email;
    @NotBlank
    private String referenceNumber;
    @NotBlank
    private String requestStatus;
    private String declineReason;
    private String approveStatus;

    private int gmailSyncRequestCount;
}
