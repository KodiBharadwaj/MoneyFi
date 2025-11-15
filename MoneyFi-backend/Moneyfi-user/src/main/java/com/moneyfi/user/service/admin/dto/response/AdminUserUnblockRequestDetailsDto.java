package com.moneyfi.user.service.admin.dto.response;

import com.moneyfi.user.util.enums.RaiseRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserUnblockRequestDetailsDto {
    private String unblockRequestReason;
    private String referenceNumber;
    private Integer daysTakenForCompletion;
    private Map<String, String> approvedOrRejected = new HashMap<>();
    private RaiseRequestStatus requestStatus;
    private String blockedBy;
    private Map<RaiseRequestStatus, UserRequestsUpdatedHistDto> requestTimeStatusHistory = new HashMap<>();
}
