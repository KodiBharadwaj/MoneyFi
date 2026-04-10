package com.moneyfi.transaction.service.general;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ManualBatchScheduling {

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

    public void enableRecurringSyncUsingSpringBatch(String type) {
        if (type.equalsIgnoreCase("income")) {
            try {
                log.info("Running Adding Recurring Incomes Job...");
                jobLauncher.run(incomeJob, new JobParametersBuilder()
                        .addLong("time", System.currentTimeMillis())
                        .toJobParameters());
            } catch (Exception e) {
                log.error("Recurring Incomes job failed", e);
                e.printStackTrace();
            }
        } else if (type.equalsIgnoreCase("expense")) {
            try {
                log.info("Running Adding Recurring Expenses Job...");
                jobLauncher.run(expenseJob, new JobParametersBuilder()
                        .addLong("time", System.currentTimeMillis() + 1)
                        .toJobParameters());
            } catch (Exception e) {
                log.error("Recurring Expenses job failed", e);
                e.printStackTrace();
            }
        } else if (type.equalsIgnoreCase("goal")) {
            try {
                log.info("Running Adding Recurring Goal Job...");
                jobLauncher.run(goalJob, new JobParametersBuilder()
                        .addLong("time", System.currentTimeMillis() + 1)
                        .toJobParameters());
            } catch (Exception e) {
                log.error("Recurring Expenses job failed", e);
                e.printStackTrace();
            }
        }
    }
}
