package com.moneyfi.transaction.batch.service;

import com.moneyfi.constants.enums.TransactionServiceType;
import com.moneyfi.transaction.batch.config.AuditContext;
import com.moneyfi.transaction.batch.entity.BatchJobDetailsAddon;
import com.moneyfi.transaction.batch.repository.BatchJobDetailsAddonRepository;
import com.moneyfi.transaction.exceptions.ScenarioNotPossibleException;
import com.moneyfi.transaction.repository.transaction.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import static com.moneyfi.transaction.utils.constants.StringConstants.TIME;

@Service
@Slf4j
@RequiredArgsConstructor
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

    private final BatchJobDetailsAddonRepository batchJobDetailsAddonRepository;
    private final TransactionRepository transactionRepository;

    public void triggerBatchJob(TransactionServiceType type, Long adminUserId, String username) {
        Long userId = null;
        if (StringUtils.isNotBlank(username)) {
            userId = transactionRepository.getUserIdFromUsername(username);
            if (ObjectUtils.isEmpty(userId))
                throw new ScenarioNotPossibleException("Invalid username entered/unauthorized");
        }

        if (type.name().equalsIgnoreCase(TransactionServiceType.INCOME.name())) {
            try {
                log.info("Running Adding Recurring Incomes Job...");

                JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();

                jobParametersBuilder.addLong(TIME, System.currentTimeMillis());
                if (userId != null) jobParametersBuilder.addLong("userId", userId);

                jobLauncher.run(incomeJob, jobParametersBuilder.toJobParameters());
            } catch (Exception e) {
                log.error("Recurring Incomes job failed", e);
                e.printStackTrace();
            }
        } else if (type.name().equalsIgnoreCase(TransactionServiceType.EXPENSE.name())) {
            try {
                log.info("Running Adding Recurring Expenses Job...");

                JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();

                jobParametersBuilder.addLong(TIME, System.currentTimeMillis() + 1);
                if (userId != null) jobParametersBuilder.addLong("userId", userId);

                jobLauncher.run(incomeJob, jobParametersBuilder.toJobParameters());
            } catch (Exception e) {
                log.error("Recurring Expenses job failed", e);
                e.printStackTrace();
            }
        } else if (type.name().equalsIgnoreCase(TransactionServiceType.GOAL.name())) {
            try {
                log.info("Running Adding Recurring Goal Job...");

                JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();

                jobParametersBuilder.addLong(TIME, System.currentTimeMillis());
                if (userId != null) jobParametersBuilder.addLong("userId", userId);

                jobLauncher.run(incomeJob, jobParametersBuilder.toJobParameters());
            } catch (Exception e) {
                log.error("Recurring Expenses job failed", e);
                e.printStackTrace();
            }
        }

        if (adminUserId != null) {
            try {
                log.info("Admin user id: {}", adminUserId);

                AuditContext.setCurrentUser(adminUserId);

                batchJobDetailsAddonRepository.save(
                        BatchJobDetailsAddon.builder()
                                .jobType(type.name())
                                .build()
                );

            } finally {
                AuditContext.clear();
            }
        }
    }
}
