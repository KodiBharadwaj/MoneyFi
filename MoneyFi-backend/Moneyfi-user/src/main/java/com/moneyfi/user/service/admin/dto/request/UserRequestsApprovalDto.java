package com.moneyfi.user.service.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
