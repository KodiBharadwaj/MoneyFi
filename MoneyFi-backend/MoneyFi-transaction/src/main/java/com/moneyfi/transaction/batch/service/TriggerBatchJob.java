package com.moneyfi.transaction.batch.service;

import com.moneyfi.constants.enums.TransactionServiceType;
import jakarta.validation.constraints.NotNull;

public interface TriggerBatchJob {
    void triggerBatchJob(TransactionServiceType type, Long adminUserId, String username, String token);

    void triggerBatchForSchedulingNotification(Long adminUserId, @NotNull Long scheduleId);
}
