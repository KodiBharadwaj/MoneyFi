package com.moneyfi.transaction.batch.scheduler;

import com.moneyfi.constants.enums.TransactionServiceType;
import com.moneyfi.transaction.batch.service.TriggerBatchJob;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.scheduler.enabled", havingValue = "true")
public class BatchScheduler {

    @Autowired
    private JobLauncher jobLauncher;

    private final TriggerBatchJob triggerBatchJob;

    @PostConstruct
    public void initializeScheduledMethodsInCaseOfServiceRunningDelay() throws Exception {
        runMonthlyBatchJob();
        runDailyBatchJob();
    }

    @Scheduled(cron = "0 0 0 1 * *")
    public void runMonthlyBatchJob() throws Exception {
        triggerBatchJob.triggerBatchJob(TransactionServiceType.INCOME.name());
        triggerBatchJob.triggerBatchJob(TransactionServiceType.EXPENSE.name());
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void runDailyBatchJob() throws Exception {
        triggerBatchJob.triggerBatchJob(TransactionServiceType.GOAL.name());
    }
}
