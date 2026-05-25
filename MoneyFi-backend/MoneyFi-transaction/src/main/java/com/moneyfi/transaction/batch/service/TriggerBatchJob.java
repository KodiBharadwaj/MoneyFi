package com.moneyfi.transaction.batch.service;

import com.moneyfi.constants.enums.TransactionServiceType;

public interface TriggerBatchJob {
    void triggerBatchJob(TransactionServiceType type, Long adminUserId, String username, String token);
}
