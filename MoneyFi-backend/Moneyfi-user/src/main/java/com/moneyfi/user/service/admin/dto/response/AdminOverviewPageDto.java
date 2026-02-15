package com.moneyfi.user.service.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminOverviewPageDto {
    private Long activeUsers;
    private Long blockedUsers;
    private Long deletedUsers;
    private Long totalUsers;

    private Long accountUnblockRequests;
    private Long nameChangeRequests;
    private Long accountReactivateRequests;
    private Long otherRequests;

    private Long userDefectRaises;
    private Long userFeedbacks;
}
