package com.moneyfi.transaction.batch.listener.expense;

import com.moneyfi.constants.dto.BatchInfoForEmailDto;
import com.moneyfi.constants.enums.TransactionServiceType;
import com.moneyfi.transaction.batch.service.general.BatchAuthTokenStore;
import com.moneyfi.transaction.model.expense.ExpenseModel;
import com.moneyfi.transaction.model.income.IncomeModel;
import com.moneyfi.transaction.service.external.api.ExternalApiCallService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.moneyfi.transaction.utils.constants.StringConstants.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpenseJobListener implements JobExecutionListener {

    private final ExternalApiCallService externalApiCallService;
    private final BatchAuthTokenStore batchAuthTokenStore;

    @Override
    public void afterJob(JobExecution jobExecution) {

        if (jobExecution.getStatus() != BatchStatus.COMPLETED) {
            return;
        }

        Long userId = jobExecution.getJobParameters().getLong(USER_ID);

        if (userId == null) {
            return;
        }

        for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
            List<ExpenseModel> expenses = (List<ExpenseModel>) stepExecution.getExecutionContext().get(PROCESSED_EXPENSES);

            if (expenses == null) {
                continue;
            }

            List<BatchInfoForEmailDto> batchInfoList = new ArrayList<>();
            for (ExpenseModel expense : expenses) {
                BatchInfoForEmailDto dto = new BatchInfoForEmailDto();
                dto.setUserId(userId);
                dto.setTransactionType(TransactionServiceType.EXPENSE.name());
                dto.setDescription(expense.getDescription());
                dto.setAmount(expense.getAmount());
                batchInfoList.add(dto);
            }

            String requestId = jobExecution.getJobParameters().getString(REQUEST_ID);
            String authToken = batchAuthTokenStore.get(requestId);

            log.info("Verifying admin token: {}", authToken);
            if (authToken != null) {
                batchAuthTokenStore.remove(requestId);
                externalApiCallService.externalCallToUserServiceToSendEmailToUser(batchInfoList, authToken);
            }
        }
    }
}
