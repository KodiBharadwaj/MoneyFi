package com.moneyfi.transaction.batch.service;

import jakarta.validation.constraints.NotBlank;

public interface TriggerBatchJob {
    void triggerBatchJob(@NotBlank String type);
}
