package com.moneyfi.transaction.batch.service;

import com.moneyfi.constants.enums.TransactionServiceType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static com.moneyfi.transaction.utils.constants.StringConstants.TIME;

@Service
@Slf4j
public class TriggerBatchJobImpl implements TriggerBatchJob {
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

    public void triggerBatchJob(String type) {
        if (type.equalsIgnoreCase(TransactionServiceType.INCOME.name())) {
            try {
                log.info("Running Adding Recurring Incomes Job...");

                jobLauncher.run(incomeJob, new JobParametersBuilder()
                        .addLong(TIME, System.currentTimeMillis())
                        .toJobParameters());
            } catch (Exception e) {
                log.error("Recurring Incomes job failed", e);
                e.printStackTrace();
            }
        } else if (type.equalsIgnoreCase(TransactionServiceType.EXPENSE.name())) {
            try {
                log.info("Running Adding Recurring Expenses Job...");

                jobLauncher.run(expenseJob, new JobParametersBuilder()
                        .addLong(TIME, System.currentTimeMillis() + 1)
                        .toJobParameters());
            } catch (Exception e) {
                log.error("Recurring Expenses job failed", e);
                e.printStackTrace();
            }
        } else if (type.equalsIgnoreCase(TransactionServiceType.GOAL.name())) {
            try {
                log.info("Running Adding Recurring Goal Job...");

                jobLauncher.run(goalJob, new JobParametersBuilder()
                        .addLong(TIME, System.currentTimeMillis() + 1)
                        .toJobParameters());
            } catch (Exception e) {
                log.error("Recurring Expenses job failed", e);
                e.printStackTrace();
            }
        }
    }
}
