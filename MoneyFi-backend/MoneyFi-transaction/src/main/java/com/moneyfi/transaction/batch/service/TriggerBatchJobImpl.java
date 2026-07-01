package com.moneyfi.transaction.batch.service;

import com.moneyfi.constants.enums.TransactionServiceType;
import com.moneyfi.transaction.batch.service.general.BatchAuthTokenStore;
import com.moneyfi.transaction.exceptions.ScenarioNotPossibleException;
import com.moneyfi.transaction.repository.transaction.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.moneyfi.transaction.utils.constants.StringConstants.*;
import static com.moneyfi.constants.constants.CommonConstants.USER_ID;

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

    private final TransactionRepository transactionRepository;
    private final BatchAuthTokenStore batchAuthTokenStore;

    public void triggerBatchJob(TransactionServiceType type, Long adminUserId, String username, String token) {
        Long userId = null;
        if (StringUtils.isNotBlank(username)) {
            userId = transactionRepository.getUserIdFromUsername(username);
            if (ObjectUtils.isEmpty(userId))
                throw new ScenarioNotPossibleException("Invalid username entered/unauthorized");
        }

        if (type.name().equalsIgnoreCase(TransactionServiceType.INCOME.name())) {
            try {
                log.info("Running Adding Recurring Incomes Job...");
                jobLauncher.run(incomeJob, addJobParameters(userId, adminUserId, System.currentTimeMillis(), token));
            } catch (Exception e) {
                log.error("Recurring Incomes job failed", e);
                e.printStackTrace();
            }
        } else if (type.name().equalsIgnoreCase(TransactionServiceType.EXPENSE.name())) {
            try {
                log.info("Running Adding Recurring Expenses Job...");
                jobLauncher.run(expenseJob, addJobParameters(userId, adminUserId, System.currentTimeMillis() + 1, token));
            } catch (Exception e) {
                log.error("Recurring Expenses job failed", e);
                e.printStackTrace();
            }
        } else if (type.name().equalsIgnoreCase(TransactionServiceType.GOAL.name())) {
            try {
                log.info("Running Adding Recurring Goal Job...");
                jobLauncher.run(goalJob, addJobParameters(userId, adminUserId, System.currentTimeMillis(), token));
            } catch (Exception e) {
                log.error("Recurring Expenses job failed", e);
                e.printStackTrace();
            }
        }
    }

    private JobParameters addJobParameters(Long userId, Long adminUserId, Long time, String token) {
        String requestId = UUID.randomUUID().toString();
        if (userId != null && token != null) batchAuthTokenStore.put(requestId, token);

        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
        jobParametersBuilder.addLong(TIME, time);
        jobParametersBuilder.addString(REQUEST_ID, requestId);
        if (userId != null) jobParametersBuilder.addLong(USER_ID, userId);
        if (adminUserId != null) jobParametersBuilder.addString(ADMIN_USER_ID, ADMIN + UNDERSCORE + adminUserId);
        else jobParametersBuilder.addString(ADMIN_USER_ID, BATCH_AUTO_TRIGGER);
        return jobParametersBuilder.toJobParameters();
    }
}
