package com.moneyfi.transaction.batch.service;

import com.moneyfi.constants.enums.TransactionServiceType;

import java.time.LocalDate;

public interface TriggerBatchJob {
    void triggerBatchJob(TransactionServiceType type, Long adminUserId, String username, String token, LocalDate date);
    void triggerBatchJob(TransactionServiceType type);
}
