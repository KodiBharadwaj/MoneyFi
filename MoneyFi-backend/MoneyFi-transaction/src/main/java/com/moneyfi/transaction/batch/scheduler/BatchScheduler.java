package com.moneyfi.transaction.batch.scheduler;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.scheduler.enabled", havingValue = "true")
public class BatchScheduler {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("incomeJob")
    private Job incomeJob;

    @Autowired
    @Qualifier("expenseJob")
    private Job expenseJob;

    @Autowired
    @Qualifier("goalJob")
    private Job goalJob;

    @PostConstruct
    public void initializeScheduledMethodsInCaseOfServiceRunningDelay() throws Exception {
        runMonthlyBatchJob();
        runDailyBatchJob();
    }

    @Scheduled(cron = "0 0 0 1 * *")
    public void runMonthlyBatchJob() throws Exception {
        try {
            log.info("Running Adding Recurring Incomes Job...");
            jobLauncher.run(incomeJob, new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters());
        } catch (Exception e) {
            log.error("Recurring Incomes job failed", e);
            e.printStackTrace();
        }

//        try {
//            log.info("Running Adding Recurring Expenses Job...");
//            jobLauncher.run(expenseJob, new JobParametersBuilder()
//                    .addLong("time", System.currentTimeMillis() + 1)
//                    .toJobParameters());
//        } catch (Exception e) {
//            log.error("Recurring Expenses job failed", e);
//            e.printStackTrace();
//        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void runDailyBatchJob() throws Exception {
        try {
            log.info("Running Adding Recurring Goals Job...");
            jobLauncher.run(goalJob, new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters());
        } catch (Exception e) {
            log.error("Recurring Goals job failed", e);
            e.printStackTrace();
        }
    }
}
