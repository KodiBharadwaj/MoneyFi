package com.moneyfi.transaction.batch.scheduler;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
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
    private Job job;

    @PostConstruct
    public void initializeScheduledMethodsInCaseOfServiceRunningDelay() throws Exception {
        runBatchJob();
    }

    @Scheduled(cron = "0 0 0 1 * *")
    public void runBatchJob() throws Exception {
        log.info("Job triggered for adding Recurring Incomes!");
        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(job, params);
    }
}
